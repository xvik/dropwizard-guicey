package ru.vyarus.dropwizard.guice.module;

import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import io.dropwizard.core.cli.Command;
import io.dropwizard.core.setup.Bootstrap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vyarus.dropwizard.guice.bundle.GuiceyBundleLookup;
import ru.vyarus.dropwizard.guice.module.context.ConfigItem;
import ru.vyarus.dropwizard.guice.module.context.ConfigurationContext;
import ru.vyarus.dropwizard.guice.module.context.option.Options;
import ru.vyarus.dropwizard.guice.module.installer.CoreInstallersBundle;
import ru.vyarus.dropwizard.guice.module.installer.FeatureInstaller;
import ru.vyarus.dropwizard.guice.module.installer.internal.CommandSupport;
import ru.vyarus.dropwizard.guice.module.installer.internal.ExtensionsHolder;
import ru.vyarus.dropwizard.guice.module.installer.internal.ExtensionsSupport;
import ru.vyarus.dropwizard.guice.module.installer.option.WithOptions;
import ru.vyarus.dropwizard.guice.module.installer.order.OrderComparator;
import ru.vyarus.dropwizard.guice.module.installer.scanner.ClassVisitor;
import ru.vyarus.dropwizard.guice.module.installer.scanner.ClasspathScanner;
import ru.vyarus.dropwizard.guice.module.installer.util.BundleSupport;
import ru.vyarus.dropwizard.guice.module.installer.util.FeatureUtils;
import ru.vyarus.dropwizard.guice.module.installer.util.InstanceUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static ru.vyarus.dropwizard.guice.GuiceyOptions.ScanPackages;
import static ru.vyarus.dropwizard.guice.GuiceyOptions.SearchCommands;
import static ru.vyarus.dropwizard.guice.GuiceyOptions.ScanProtectedClasses;
import static ru.vyarus.dropwizard.guice.GuiceyOptions.UseCoreInstallers;
import static ru.vyarus.dropwizard.guice.module.context.stat.Stat.BundleResolutionTime;
import static ru.vyarus.dropwizard.guice.module.context.stat.Stat.BundleTime;
import static ru.vyarus.dropwizard.guice.module.context.stat.Stat.ConfigurationTime;
import static ru.vyarus.dropwizard.guice.module.context.stat.Stat.ExtensionsRecognitionTime;
import static ru.vyarus.dropwizard.guice.module.context.stat.Stat.GuiceyBundleInitTime;
import static ru.vyarus.dropwizard.guice.module.context.stat.Stat.GuiceyTime;
import static ru.vyarus.dropwizard.guice.module.context.stat.Stat.InstallersTime;

/**
 * Guicey initialization logic performed under dropwizard configuration phase.
 * <ul>
 * <li>Bundles lookup and initialization</li>
 * <li>Classpath scan:
 * <ul>
 * <li>Commands search</li>
 * <li>Installers search</li>
 * <li>Extensions search</li>
 * </ul>
 * </li>
 * </ul>
 *
 * @author Vyacheslav Rusakov
 * @since 14.06.2019
 */
@SuppressWarnings("PMD.ExcessiveImports")
public class GuiceyInitializer {
    /**
     * Special package name for classpath scan configuration to use application location package.
     */
    public static final String APP_PKG = "<app>";
    private static final OrderComparator COMPARATOR = new OrderComparator();
    private final Logger logger = LoggerFactory.getLogger(GuiceyInitializer.class);

    private final Stopwatch guiceyTimer;
    private final Stopwatch confTimer;

    private final Bootstrap bootstrap;
    private final ConfigurationContext context;
    private final ClasspathScanner scanner;

    public GuiceyInitializer(final Bootstrap bootstrap, final ConfigurationContext context) {
        guiceyTimer = context.stat().timer(GuiceyTime);
        confTimer = context.stat().timer(ConfigurationTime);

        // this will also trigger registered dropwizard bundles initialization
        // (so dropwizard bundles init before guicey bundles)
        context.initPhaseStarted(bootstrap);

        this.bootstrap = bootstrap;
        this.context = context;
        final String[] packages = context.option(ScanPackages);
        final boolean acceptProtected = context.option(ScanProtectedClasses);
        // configuration shortcut for all packages starting from application location
        if (packages.length == 1 && APP_PKG.equals(packages[0])) {
            packages[0] = bootstrap.getApplication().getClass().getPackage().getName();
        }
        // classpath scan performed immediately (if required)
        this.scanner = packages.length > 0
                ? new ClasspathScanner(
                        Sets.newHashSet(Arrays.asList(packages)), acceptProtected, context.stat()) : null;
    }

    /**
     * Resolve bundles and initialize.
     *
     * @param bundleLookup bundle lookup object
     */
    public void initializeBundles(final GuiceyBundleLookup bundleLookup) {
        final Stopwatch timer = context.stat().timer(BundleTime);
        final Stopwatch resolutionTimer = context.stat().timer(BundleResolutionTime);
        if (context.option(UseCoreInstallers)) {
            context.registerBundles(new CoreInstallersBundle());
        }
        context.registerLookupBundles(bundleLookup.lookup());
        resolutionTimer.stop();
        final Stopwatch btime = context.stat().timer(GuiceyBundleInitTime);
        BundleSupport.initBundles(context);
        btime.stop();
        timer.stop();
    }

    /**
     * Perform classpath scan to resolve commands.
     */
    public void findCommands() {
        final boolean searchCommands = context.option(SearchCommands);
        if (searchCommands) {
            Preconditions.checkState(scanner != null,
                    "Commands search could not be performed, because auto scan was not activated");

            final List<Command> installed = CommandSupport.registerCommands(bootstrap, scanner, context);
            context.lifecycle().commandsResolved(installed);
        }
    }

    /**
     * Perform classpath scan to find installers. Create enabled installer instances.
     */
    public void resolveInstallers() {
        final Stopwatch timer = context.stat().timer(InstallersTime);
        final List<Class<? extends FeatureInstaller>> installerClasses = findInstallers();
        final List<FeatureInstaller> installers = prepareInstallers(installerClasses);
        context.installersResolved(installers);
        timer.stop();
    }

    /**
     * Performs classpath scan to search for extensions. Register all extensions (note that extensions may be disabled
     * on run phase).
     */
    @SuppressWarnings("PMD.PrematureDeclaration")
    public void resolveExtensions() {
        final Stopwatch itimer = context.stat().timer(InstallersTime);
        final Stopwatch timer = context.stat().timer(ExtensionsRecognitionTime);
        final ExtensionsHolder holder = context.getExtensionsHolder();
        final List<Class<?>> manual = context.getEnabledExtensions();
        for (Class<?> type : manual) {
            if (!ExtensionsSupport.registerExtension(context, type, false)) {
                throw new IllegalStateException("No installer found for extension " + type.getName()
                        + ". Available installers: " + holder.getInstallerTypes()
                        .stream().map(FeatureUtils::getInstallerExtName).collect(Collectors.joining(", ")));
            }
        }
        context.lifecycle().manualExtensionsValidated(context.getItems(ConfigItem.Extension), manual);
        if (scanner != null) {
            final List<Class<?>> extensions = new ArrayList<>();
            scanner.scan(type -> {
                if (manual.contains(type)) {
                    // avoid duplicate extension installation, but register it's appearance in auto scan scope
                    context.getOrRegisterExtension(type, true);
                    extensions.add(type);
                } else {
                    // if matching installer found - extension recognized, otherwise - not an extension
                    if (ExtensionsSupport.registerExtension(context, type, true)) {
                        extensions.add(type);
                    }
                }
            });
            context.lifecycle().classpathExtensionsResolved(extensions);
        }
        timer.stop();
        itimer.stop();
    }

    /**
     * Init lifecycle end. Flush classpath scan cache.
     */
    public void initFinished() {
        if (scanner != null) {
            scanner.cleanup();
        }
        context.lifecycle().initialized();

        confTimer.stop();
        guiceyTimer.stop();
    }

    /**
     * Performs classpath scan to find all classes implementing or use only manually configured installers.
     * {@link FeatureInstaller}.
     *
     * @return list of found installers or empty list
     */
    @SuppressWarnings("unchecked")
    private List<Class<? extends FeatureInstaller>> findInstallers() {
        if (scanner != null) {
            final List<Class<? extends FeatureInstaller>> installers = Lists.newArrayList();
            scanner.scan(new ClassVisitor() {
                @Override
                public void visit(final Class<?> type) {
                    if (FeatureUtils.is(type, FeatureInstaller.class)) {
                        installers.add((Class<? extends FeatureInstaller>) type);
                    }
                }
            });
            // sort to unify registration order on different systems
            installers.sort(Comparator.comparing(Class::getName));
            context.registerInstallersFromScan(installers);
        }
        final List<Class<? extends FeatureInstaller>> installers = context.getEnabledInstallers();
        installers.sort(COMPARATOR);
        logger.debug("Found {} installers", installers.size());
        return installers;
    }


    /**
     * Instantiate all found installers using default constructor.
     *
     * @param installerClasses found installer classes
     * @return list of installer instances
     */
    private List<FeatureInstaller> prepareInstallers(
            final List<Class<? extends FeatureInstaller>> installerClasses) {
        final List<FeatureInstaller> installers = Lists.newArrayList();
        // different instance then used in guice context, but it's just an accessor object
        final Options options = new Options(context.options());
        for (Class<? extends FeatureInstaller> installerClass : installerClasses) {
            try {
                final FeatureInstaller installer = InstanceUtils.create(installerClass);
                installers.add(installer);
                if (WithOptions.class.isAssignableFrom(installerClass)) {
                    ((WithOptions) installer).setOptions(options);
                }
            } catch (Exception e) {
                throw new IllegalStateException("Failed to register installer " + installerClass.getName(), e);
            }
        }
        return installers;
    }


}
