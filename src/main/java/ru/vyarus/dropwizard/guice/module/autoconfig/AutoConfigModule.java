package ru.vyarus.dropwizard.guice.module.autoconfig;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.AbstractModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vyarus.dropwizard.guice.module.autoconfig.feature.FeatureInstaller;
import ru.vyarus.dropwizard.guice.module.autoconfig.feature.FeatureInstallerExecutor;
import ru.vyarus.dropwizard.guice.module.autoconfig.feature.FeaturesHolder;
import ru.vyarus.dropwizard.guice.module.autoconfig.scanner.ClassVisitor;
import ru.vyarus.dropwizard.guice.module.autoconfig.scanner.ClasspathScanner;
import ru.vyarus.dropwizard.guice.module.autoconfig.util.FeatureUtils;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

/**
 * Module performs auto configuration using classpath scanning.
 * First search provided packages for registered installers
 * {@link ru.vyarus.dropwizard.guice.module.autoconfig.feature.FeatureInstaller}. Then scan classpath obe more time
 * with installers to apply extensions.
 * <p>Feature installers can be disabled from bundle config.</p>
 * NOTE: Classpath scan will load all found classes in configured packages. Try to reduce scan scope
 * as much as possible.
 */
public class AutoConfigModule extends AbstractModule {
    private final Logger logger = LoggerFactory.getLogger(AutoConfigModule.class);
    private final ClasspathScanner scanner;
    private final List<Class<? extends FeatureInstaller>> disabledInstallers;

    public AutoConfigModule(final ClasspathScanner scanner,
                            final List<Class<? extends FeatureInstaller>> disabledInstallers) {
        this.scanner = scanner;
        this.disabledInstallers = disabledInstallers;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void configure() {
        bind(FeatureInstallerExecutor.class).asEagerSingleton();

        final Set<Class<? extends FeatureInstaller>> installerClasses = findInstallers();
        final List<FeatureInstaller> installers = prepareInstallers(installerClasses);

        final FeaturesHolder holder = new FeaturesHolder(installers);
        bind(FeaturesHolder.class).toInstance(holder);
        lookupFeatures(holder);
    }

    /**
     * Performs classpath scan to find all classes implementing
     * {@link ru.vyarus.dropwizard.guice.module.autoconfig.feature.FeatureInstaller}.
     *
     * @return set of found installers or empty list
     */
    @SuppressWarnings("unchecked")
    private Set<Class<? extends FeatureInstaller>> findInstallers() {
        final List<Class<? extends FeatureInstaller>> installers = Lists.newArrayList();
        scanner.scan(new ClassVisitor() {
            @Override
            public void visit(final Class<?> type) {
                if (FeatureUtils.is(type, FeatureInstaller.class)) {
                    installers.add((Class<? extends FeatureInstaller>) type);
                }
            }
        });

        final Set<Class<? extends FeatureInstaller>> validInstallers = Sets.newHashSet(
                Iterables.filter(installers, new Predicate<Class<? extends FeatureInstaller>>() {
                    @Override
                    public boolean apply(@Nullable final Class<? extends FeatureInstaller> input) {
                        return !disabledInstallers.contains(input);
                    }
                }));

        Preconditions.checkState(!validInstallers.isEmpty(),
                "No feature installers found. Make sure you configure "
                + "proper packages for classpath scanning");
        logger.debug("Found {} feature installers", validInstallers.size());
        return validInstallers;
    }

    /**
     * Instantiate all found installers using default constructor.
     *
     * @param installerClasses found installer classes
     * @return list of installer instances
     */
    private List<FeatureInstaller> prepareInstallers(
            final Set<Class<? extends FeatureInstaller>> installerClasses) {
        final List<FeatureInstaller> installers = Lists.newArrayList();
        for (Class<? extends FeatureInstaller> installerClass : installerClasses) {
            try {
                final FeatureInstaller installer = installerClass.newInstance();
                installers.add(installer);
                logger.trace("Registered feature installer: {}",
                        FeatureUtils.getInstallerExtName(installerClass));
            } catch (Exception e) {
                throw new IllegalStateException("Failed to register installer "
                        + installerClass.getName(), e);
            }
        }
        return installers;
    }

    /**
     * Performs one more classpath scan to search for extensions.
     *
     * @param holder holder to store found extension classes until injector creation
     */
    @SuppressWarnings("unchecked")
    private void lookupFeatures(final FeaturesHolder holder) {
        scanner.scan(new ClassVisitor() {
            @Override
            public void visit(final Class<?> type) {
                for (FeatureInstaller installer : holder.getInstallers()) {
                    if (installer.matches(type)) {
                        logger.trace("{} extension found: {}",
                                FeatureUtils.getInstallerExtName(installer.getClass()), type.getName());
                        holder.register(installer, type);
                        // register bean in guice
                        // todo check with 2 extensions (double registration allowed?)
                        binder().bind(type);
                    }
                }
            }
        });
    }
}
