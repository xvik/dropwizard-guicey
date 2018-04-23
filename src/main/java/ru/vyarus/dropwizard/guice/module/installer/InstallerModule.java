package ru.vyarus.dropwizard.guice.module.installer;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.google.inject.AbstractModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vyarus.dropwizard.guice.module.context.ConfigurationContext;
import ru.vyarus.dropwizard.guice.module.context.info.ExtensionItemInfo;
import ru.vyarus.dropwizard.guice.module.context.info.impl.ExtensionItemInfoImpl;
import ru.vyarus.dropwizard.guice.module.context.option.Options;
import ru.vyarus.dropwizard.guice.module.context.stat.Stat;
import ru.vyarus.dropwizard.guice.module.installer.install.binding.BindingInstaller;
import ru.vyarus.dropwizard.guice.module.installer.install.binding.LazyBinding;
import ru.vyarus.dropwizard.guice.module.installer.internal.ExtensionsHolder;
import ru.vyarus.dropwizard.guice.module.installer.internal.FeatureInstallerExecutor;
import ru.vyarus.dropwizard.guice.module.installer.option.WithOptions;
import ru.vyarus.dropwizard.guice.module.installer.order.OrderComparator;
import ru.vyarus.dropwizard.guice.module.installer.scanner.ClassVisitor;
import ru.vyarus.dropwizard.guice.module.installer.scanner.ClasspathScanner;
import ru.vyarus.dropwizard.guice.module.installer.util.FeatureUtils;
import ru.vyarus.dropwizard.guice.module.installer.util.JerseyBinding;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static ru.vyarus.dropwizard.guice.module.context.stat.Stat.InstallersTime;

/**
 * Module performs auto configuration using classpath scanning or manually predefined installers and beans.
 * First search provided packages for registered installers
 * {@link FeatureInstaller}. Then scan classpath one more time with installers to apply extensions.
 * <p>
 * Feature installers can be disabled from bundle config.
 * <p>
 * NOTE: Classpath scan will load all found classes in configured packages. Try to reduce scan scope
 * as much as possible.
 *
 * @author Vyacheslav Rusakov
 * @since 31.08.2014
 */
public class InstallerModule extends AbstractModule {
    private static final OrderComparator COMPARATOR = new OrderComparator();
    private final Logger logger = LoggerFactory.getLogger(InstallerModule.class);
    private final ClasspathScanner scanner;
    private final ConfigurationContext context;

    public InstallerModule(final ClasspathScanner scanner,
                           final ConfigurationContext context) {
        this.scanner = scanner;
        this.context = context;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void configure() {
        // called just after injector creation to process instance installers
        bind(FeatureInstallerExecutor.class).asEagerSingleton();

        final Stopwatch timer = context.stat().timer(InstallersTime);
        final List<Class<? extends FeatureInstaller>> installerClasses = findInstallers();
        final List<FeatureInstaller> installers = prepareInstallers(installerClasses);
        context.lifecycle().installersResolved(new ArrayList<>(installers), context.getDisabledInstallers());
        timer.stop();

        final ExtensionsHolder holder = new ExtensionsHolder(installers, context.stat(), context.lifecycle());
        bind(ExtensionsHolder.class).toInstance(holder);

        resolveExtensions(holder);
        context.finalizeConfiguration();
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
                if (logger.isTraceEnabled()) {
                    logger.trace("Registered installer: {}", FeatureUtils.getInstallerExtName(installerClass));
                }
            } catch (Exception e) {
                throw new IllegalStateException("Failed to register installer " + installerClass.getName(), e);
            }
        }
        return installers;
    }

    /**
     * Performs one more classpath scan to search for extensions or simply install manually provided extension classes.
     *
     * @param holder holder to store found extension classes until injector creation
     */
    @SuppressWarnings("PMD.PrematureDeclaration")
    private void resolveExtensions(final ExtensionsHolder holder) {
        final Stopwatch timer = context.stat().timer(Stat.ExtensionsRecognitionTime);
        final List<Class<?>> manual = context.getEnabledExtensions();
        for (Class<?> type : manual) {
            if (!processType(type, holder, false)) {
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
                    processType(type, holder, true);
                }
            });
        }
        context.lifecycle().extensionsResolved(context.getEnabledExtensions(), context.getDisabledExtensions());
        timer.stop();
    }

    private boolean processType(final Class<?> type, final ExtensionsHolder holder, final boolean fromScan) {
        final FeatureInstaller installer = findInstaller(type, holder);
        final boolean recognized = installer != null;
        if (recognized) {
            // important to force config creation for extension from scan to allow disabling by matcher
            final ExtensionItemInfoImpl info = context.getOrRegisterExtension(type, fromScan);
            info.setLazy(type.isAnnotationPresent(LazyBinding.class));
            info.setHk2Managed(JerseyBinding.isHK2Managed(type));
            info.setInstalledBy(installer.getClass());

            // extension from scan could be disabled by matcher
            if (!fromScan || context.isExtensionEnabled(type)) {
                bindExtension(info, installer, holder);
            }
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

    /**
     * Bind extension to guice context.
     *
     * @param item      extension item descriptor
     * @param installer detected extension installer
     * @param holder    extensions holder bean
     */
    @SuppressWarnings("unchecked")
    private void bindExtension(final ExtensionItemInfo item, final FeatureInstaller installer,
                               final ExtensionsHolder holder) {
        final Class<? extends FeatureInstaller> installerClass = installer.getClass();
        final Class<?> type = item.getType();
        if (logger.isTraceEnabled()) {
            logger.trace("{} extension found: {}",
                    FeatureUtils.getInstallerExtName(installerClass), type.getName());
        }
        holder.register(installerClass, type);
        if (installer instanceof BindingInstaller) {
            ((BindingInstaller) installer).install(binder(), type, item.isLazy());
        } else if (!item.isLazy()) {
            // if installer isn't install binding manually, lazy simply disable registration
            binder().bind(type);
        }
    }
}
