package ru.vyarus.dropwizard.guice.module;

import com.google.common.collect.Lists;
import com.google.inject.Module;
import ru.vyarus.dropwizard.guice.module.installer.FeatureInstaller;
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBundle;
import ru.vyarus.dropwizard.guice.module.installer.internal.BundleContextHolder;
import ru.vyarus.dropwizard.guice.module.installer.internal.FeaturesHolder;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;

/**
 * Public api for internal guicey configuration info. Provides information about registered bundle types,
 * installers, extensions, disabled installers etc. Registered as guice bean and could be directly injected.
 * <p/>
 * Could to be used for configuration diagnostics or unit test checks.
 *
 * @author Vyacheslav Rusakov
 * @since 21.06.2016
 */
public class GuiceyConfigurationInfo {

    private final BundleContextHolder context;
    private final FeaturesHolder features;

    @Inject
    public GuiceyConfigurationInfo(final BundleContextHolder context, final FeaturesHolder features) {
        this.features = features;
        this.context = context;
    }

    /**
     * @return types of all installed bundles (including lookup bundles) or empty list
     */
    public List<Class<? extends GuiceyBundle>> getBundles() {
        return context.getBundles();
    }

    /**
     * @return types of bundles resolved by bundle lookup mechanism or empty list
     * @see ru.vyarus.dropwizard.guice.bundle.GuiceyBundleLookup
     */
    public List<Class<? extends GuiceyBundle>> getBundlesFromLookup() {
        return context.getBundlesFromLookup();
    }

    /**
     * @return types of all registered guice modules or empty list
     */
    public List<Class<? extends Module>> getModules() {
        return context.getModules();
    }

    /**
     * @return types of all registered installers (without disabled) or empty list
     */
    public List<Class<? extends FeatureInstaller>> getInstallers() {
        return features.getInstallerTypes();
    }

    /**
     * @return installer types, resolved by classpath scan (without disabled) or empty list
     */
    public List<Class<? extends FeatureInstaller>> getInstallersFromScan() {
        final List<Class<? extends FeatureInstaller>> types = Lists.newArrayList(features.getInstallerTypes());
        types.removeAll(context.getInstallers());
        return types;
    }

    /**
     * @return types of manually disabled installers or empty list
     */
    public List<Class<? extends FeatureInstaller>> getInstallersDisabled() {
        return context.getInstallersDisabled();
    }

    /**
     * @return extension types, resolved by classpath scan or empty list
     */
    public List<Class<?>> getExtensionsFromScan() {
        final List<Class<?>> types = getExtensions();
        types.removeAll(context.getExtensions());
        return types;
    }

    /**
     * @param installer installer type
     * @return list of extensions installed by this installer or empty list
     */
    public List<Class<?>> getExtensions(final Class<? extends FeatureInstaller> installer) {
        final List<Class<?>> res = features.getExtensions(installer);
        return res == null ? Collections.<Class<?>>emptyList() : res;
    }

    /**
     * @return all registered extension types (including resolved with classpath scan) or empty list
     */
    public List<Class<?>> getExtensions() {
        final List<Class<?>> res = Lists.newArrayList();
        for (Class<? extends FeatureInstaller> installer : getInstallers()) {
            res.addAll(getExtensions(installer));
        }
        return res;
    }
}
