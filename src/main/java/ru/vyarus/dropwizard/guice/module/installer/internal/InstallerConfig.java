package ru.vyarus.dropwizard.guice.module.installer.internal;

import com.google.common.collect.Sets;
import ru.vyarus.dropwizard.guice.module.installer.FeatureInstaller;

import java.util.Set;

/**
 * Installer configuration object.
 *
 * @author Vyacheslav Rusakov
 * @since 04.09.2014
 */
public class InstallerConfig {
    private final Set<Class<? extends FeatureInstaller>> disabledFeatures = Sets.newHashSet();
    private final Set<Class<? extends FeatureInstaller>> manualFeatures = Sets.newHashSet();
    private final Set<Class<?>> manualBeans = Sets.newHashSet();

    /**
     * @return set of disabled installers or empty set
     */
    public Set<Class<? extends FeatureInstaller>> getDisabledFeatures() {
        return disabledFeatures;
    }

    /**
     * @return set of manual installers or empty set
     */
    public Set<Class<? extends FeatureInstaller>> getManualFeatures() {
        return manualFeatures;
    }

    /**
     * @return set of manual beans or empty set
     */
    public Set<Class<?>> getManualBeans() {
        return manualBeans;
    }
}
