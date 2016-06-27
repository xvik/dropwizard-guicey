package ru.vyarus.dropwizard.guice.module.installer.internal;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.inject.Module;
import ru.vyarus.dropwizard.guice.module.installer.FeatureInstaller;
import ru.vyarus.dropwizard.guice.module.installer.bundle.BundleContext;
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBundle;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Bean holds dynamic configuration data: loaded bundles, registered guice modules, disabled and manual
 * installers.
 * <p>
 * Internal api. Use {@link ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo} instead.
 *
 * @author Vyacheslav Rusakov
 * @since 21.06.2016
 */
public class BundleContextHolder {

    private final List<Class<? extends Module>> modules;
    private final List<Class<? extends FeatureInstaller>> installersDisabled;
    private final List<Class<? extends FeatureInstaller>> installers;
    private final List<Class<?>> extensions;
    private final List<Class<? extends GuiceyBundle>> bundles;
    private final List<Class<? extends GuiceyBundle>> bundlesFromLookup;

    public BundleContextHolder(final BundleContext context) {
        modules = Lists.transform(context.modules, this.<Module>toClassFunction());
        installersDisabled = Lists.newArrayList(context.installerConfig.getDisabledInstallers());
        installers = Lists.newArrayList(context.installerConfig.getManualInstallers());
        extensions = Lists.newArrayList(context.installerConfig.getManualExtensions());
        bundles = context.installedBundles;
        bundlesFromLookup = Lists.transform(context.lookupBundles, this.<GuiceyBundle>toClassFunction());
    }

    /**
     * @return registered guice modules
     */
    public List<Class<? extends Module>> getModules() {
        return modules;
    }

    /**
     * @return disabled installer types
     */
    public List<Class<? extends FeatureInstaller>> getInstallersDisabled() {
        return installersDisabled;
    }

    /**
     * @return manually registered installer types
     */
    public List<Class<? extends FeatureInstaller>> getInstallers() {
        return installers;
    }

    /**
     * @return manually registered extensions
     */
    public List<Class<?>> getExtensions() {
        return extensions;
    }

    /**
     * @return installed guice bundle types (including lookup bundles)
     */
    public List<Class<? extends GuiceyBundle>> getBundles() {
        return bundles;
    }

    /**
     * @return guice bundle types resolved during bundle lookup
     */
    public List<Class<? extends GuiceyBundle>> getBundlesFromLookup() {
        return bundlesFromLookup;
    }

    private <T> Function<T, Class<? extends T>> toClassFunction() {
        return new Function<T, Class<? extends T>>() {
            @Nonnull
            @Override
            @SuppressWarnings("unchecked")
            public Class<? extends T> apply(@Nonnull final T input) {
                return (Class<? extends T>) input.getClass();
            }
        };
    }
}
