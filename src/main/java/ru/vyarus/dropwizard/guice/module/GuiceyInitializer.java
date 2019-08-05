package ru.vyarus.dropwizard.guice.module;

import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import io.dropwizard.cli.Command;
import io.dropwizard.setup.Bootstrap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vyarus.dropwizard.guice.bundle.GuiceyBundleLookup;
import ru.vyarus.dropwizard.guice.module.context.ConfigurationContext;
import ru.vyarus.dropwizard.guice.module.context.info.impl.ExtensionItemInfoImpl;
import ru.vyarus.dropwizard.guice.module.context.option.Options;
import ru.vyarus.dropwizard.guice.module.context.stat.Stat;
import ru.vyarus.dropwizard.guice.module.installer.CoreInstallersBundle;
import ru.vyarus.dropwizard.guice.module.installer.FeatureInstaller;
import ru.vyarus.dropwizard.guice.module.installer.install.binding.LazyBinding;
import ru.vyarus.dropwizard.guice.module.installer.internal.CommandSupport;
import ru.vyarus.dropwizard.guice.module.installer.internal.ExtensionsHolder;
import ru.vyarus.dropwizard.guice.module.installer.option.WithOptions;
import ru.vyarus.dropwizard.guice.module.installer.order.OrderComparator;
import ru.vyarus.dropwizard.guice.module.installer.scanner.ClassVisitor;
import ru.vyarus.dropwizard.guice.module.installer.scanner.ClasspathScanner;
import ru.vyarus.dropwizard.guice.module.installer.util.BundleSupport;
import ru.vyarus.dropwizard.guice.module.installer.util.FeatureUtils;
import ru.vyarus.dropwizard.guice.module.installer.util.JerseyBinding;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static ru.vyarus.dropwizard.guice.GuiceyOptions.*;
import static ru.vyarus.dropwizard.guice.module.context.stat.Stat.*;
import static ru.vyarus.dropwizard.guice.module.installer.InstallersOptions.JerseyExtensionsManagedByGuice;

/**
 * Guicey initialization logic performed under dropwizard configuration phase.
 * <ul>
 *     <li>Bundles lookup and initialization</li>
 *     <li>Classpath scan:
 *     <ul>
 *         <li>Commands search</li>
 *         <li>Installers search</li>
 *         <li>Extensions search</li>
 *     </ul>
 *     </li>
 * </ul>
 *
 * @author Vyacheslav Rusakov
 * @since 14.06.2019
 */
public class GuiceyInitializer {
    private static final OrderComparator COMPARATOR = new OrderComparator();
    private final Logger logger = LoggerFactory.getLogger(GuiceyInitializer.class);

    private final Bootstrap bootstrap;
    private final ConfigurationContext context;
    private final ClasspathScanner scanner;

    public GuiceyInitializer(final Bootstrap bootstrap, final ConfigurationContext context) {
        this.bootstrap = bootstrap;
        this.context = context;
        final String[] packages = context.option(ScanPackages);
        // classpath scan performed immediately (if required)
        this.scanner = packages.length > 0
                ? new ClasspathScanner(Sets.newHashSet(Arrays.asList(packages)), context.stat()) : null;
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
        BundleSupport.initBundles(context);
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
        final Stopwatch timer = context.stat().timer(Stat.ExtensionsRecognitionTime);
        final ExtensionsHolder holder = context.getExtensionsHolder();
        final boolean guiceFirstMode = context.option(JerseyExtensionsManagedByGuice);
        final List<Class<?>> manual = context.getEnabledExtensions();
        for (Class<?> type : manual) {
            if (!processType(type, holder, guiceFirstMode, false)) {
                throw new IllegalStateException("No installer found for extension " + type.getName()
                        + ". Available installers: " + holder.getInstallerTypes()
                        .stream().map(FeatureUtils::getInstallerExtName).collect(Collectors.joining(", ")));
            }
        }
        if (scanner != null) {
            scanner.scan(type -> {
                if (manual.contains(type)) {
                    // avoid duplicate extension installation, but register it's appearance in auto scan scope
                    context.getOrRegisterExtension(type, true);
                } else {
                    // if matching installer found - extension recognized, otherwise - not an extension
                    processType(type, holder, guiceFirstMode, true);
                }
            });
        }
        context.lifecycle().extensionsResolved(context.getEnabledExtensions(), context.getDisabledExtensions());
        timer.stop();
    }

    /**
     * Flush classpath scan cache.
     */
    public void cleanup() {
        if (scanner != null) {
            scanner.cleanup();
        }
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
                final FeatureInstaller installer = installerClass.newInstance();
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


    private boolean processType(final Class<?> type, final ExtensionsHolder holder,
                                final boolean guiceFirstMode, final boolean fromScan) {
        final FeatureInstaller installer = findInstaller(type, holder);
        final boolean recognized = installer != null;
        if (recognized) {
            // important to force config creation for extension from scan to allow disabling by matcher
            final ExtensionItemInfoImpl info = context.getOrRegisterExtension(type, fromScan);
            info.setLazy(type.isAnnotationPresent(LazyBinding.class));
            info.setJerseyManaged(JerseyBinding.isJerseyManaged(type, guiceFirstMode));
            info.setInstaller(installer);
        }
        return recognized;
    }

    /**
     * Search for matching installer. Extension may match multiple installer, but only one will be actually
     * used (note that installers are ordered)
     *
     * @param type   extension type
     * @param holder extensions holder bean
     * @return matching installer or null if no matching installer found
     */
    @SuppressWarnings("unchecked")
    private FeatureInstaller findInstaller(final Class<?> type, final ExtensionsHolder holder) {
        for (FeatureInstaller installer : holder.getInstallers()) {
            if (installer.matches(type)) {
                return installer;
            }
        }
        return null;
    }
}
