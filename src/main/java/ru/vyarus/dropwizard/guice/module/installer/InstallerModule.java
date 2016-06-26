package ru.vyarus.dropwizard.guice.module.installer;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.AbstractModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vyarus.dropwizard.guice.module.installer.install.binding.BindingInstaller;
import ru.vyarus.dropwizard.guice.module.installer.install.binding.LazyBinding;
import ru.vyarus.dropwizard.guice.module.installer.internal.FeatureInstallerExecutor;
import ru.vyarus.dropwizard.guice.module.installer.internal.FeaturesHolder;
import ru.vyarus.dropwizard.guice.module.installer.internal.InstallerConfig;
import ru.vyarus.dropwizard.guice.module.installer.order.OrderComparator;
import ru.vyarus.dropwizard.guice.module.installer.scanner.ClassVisitor;
import ru.vyarus.dropwizard.guice.module.installer.scanner.ClasspathScanner;
import ru.vyarus.dropwizard.guice.module.installer.util.FeatureUtils;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Module performs auto configuration using classpath scanning or manually predefined installers and beans.
 * First search provided packages for registered installers
 * {@link FeatureInstaller}. Then scan classpath obe more time with installers to apply extensions.
 * <p>Feature installers can be disabled from bundle config.</p>
 * NOTE: Classpath scan will load all found classes in configured packages. Try to reduce scan scope
 * as much as possible.
 */
public class InstallerModule extends AbstractModule {
    private static final OrderComparator COMPARATOR = new OrderComparator();
    private final Logger logger = LoggerFactory.getLogger(InstallerModule.class);
    private final ClasspathScanner scanner;
    private final InstallerConfig installerConfig;

    public InstallerModule(final ClasspathScanner scanner,
                           final InstallerConfig installerConfig) {
        this.scanner = scanner;
        this.installerConfig = installerConfig;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void configure() {
        // called just after injector creation to process instance installers
        bind(FeatureInstallerExecutor.class).asEagerSingleton();

        final List<Class<? extends FeatureInstaller>> installerClasses = findInstallers();
        final List<FeatureInstaller> installers = prepareInstallers(installerClasses);

        final FeaturesHolder holder = new FeaturesHolder(installers);
        bind(FeaturesHolder.class).toInstance(holder);
        resolveFeatures(holder);
    }

    /**
     * Performs classpath scan to find all classes implementing or use only manually configured installers.
     * {@link FeatureInstaller}.
     *
     * @return list of found installers or empty list
     */
    @SuppressWarnings("unchecked")
    private List<Class<? extends FeatureInstaller>> findInstallers() {
        final List<Class<? extends FeatureInstaller>> installers = Lists.newArrayList();
        if (scanner != null) {
            scanner.scan(new ClassVisitor() {
                @Override
                public void visit(final Class<?> type) {
                    if (FeatureUtils.is(type, FeatureInstaller.class)) {
                        installers.add((Class<? extends FeatureInstaller>) type);
                    }
                }
            });
        }

        installers.addAll(installerConfig.getManualInstallers());

        final Set<Class<? extends FeatureInstaller>> validInstallers = Sets.newHashSet(
                Iterables.filter(installers, new Predicate<Class<? extends FeatureInstaller>>() {
                    @Override
                    public boolean apply(@Nullable final Class<? extends FeatureInstaller> input) {
                        return !installerConfig.getDisabledInstallers().contains(input);
                    }
                }));
        installers.clear();
        installers.addAll(validInstallers);
        Collections.sort(installers, COMPARATOR);
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
        for (Class<? extends FeatureInstaller> installerClass : installerClasses) {
            try {
                final FeatureInstaller installer = installerClass.newInstance();
                installers.add(installer);
                logger.trace("Registered installer: {}",
                        FeatureUtils.getInstallerExtName(installerClass));
            } catch (Exception e) {
                throw new IllegalStateException("Failed to register installer "
                        + installerClass.getName(), e);
            }
        }
        return installers;
    }

    /**
     * Performs one more classpath scan to search for extensions or simply install manually provided extension classes.
     *
     * @param holder holder to store found extension classes until injector creation
     */
    private void resolveFeatures(final FeaturesHolder holder) {
        if (scanner != null) {
            scanner.scan(new ClassVisitor() {
                @Override
                public void visit(final Class<?> type) {
                    processType(type, holder);
                }
            });
        }
        for (Class<?> type : installerConfig.getManualExtensions()) {
            Preconditions.checkState(processType(type, holder),
                    "No installer found for extension %s", type.getName());
        }
    }

    /**
     * @param type class to analyze
     * @return true is class recognized by any installer, false otherwise
     */
    @SuppressWarnings("unchecked")
    private boolean processType(final Class<?> type, final FeaturesHolder holder) {
        boolean recognized = false;
        for (FeatureInstaller installer : holder.getInstallers()) {
            if (installer.matches(type)) {
                recognized = true;
                logger.trace("{} extension found: {}",
                        FeatureUtils.getInstallerExtName(installer.getClass()), type.getName());
                holder.register(installer.getClass(), type);
                final boolean lazy = type.isAnnotationPresent(LazyBinding.class);
                if (installer instanceof BindingInstaller) {
                    ((BindingInstaller) installer).install(binder(), type, lazy);
                } else if (!lazy) {
                    // if installer isn't install binding manually, lazy simply disable registration
                    binder().bind(type);
                }
            }
        }
        return recognized;
    }
}
