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
    private final Set<Class<? extends FeatureInstaller>> disabledInstallers = Sets.newHashSet();
    private final Set<Class<? extends FeatureInstaller>> manualInstallers = Sets.newHashSet();
    private final Set<Class<?>> manualExtensions = Sets.newHashSet();

    /**
     * @return set of disabled installers or empty set
     */
    public Set<Class<? extends FeatureInstaller>> getDisabledInstallers() {
        return disabledInstallers;
    }

    /**
     * @return set of manual installers or empty set
     */
    public Set<Class<? extends FeatureInstaller>> getManualInstallers() {
        return manualInstallers;
    }

    /**
     * @return set of manual extensions or empty set
     */
    public Set<Class<?>> getManualExtensions() {
        return manualExtensions;
    }
}
