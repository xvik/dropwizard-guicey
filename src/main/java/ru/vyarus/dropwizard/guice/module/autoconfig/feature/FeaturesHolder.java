package ru.vyarus.dropwizard.guice.module.autoconfig.feature;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;

/**
 * Bean used to hold found extensions (after scan with installers) to register them dropwizard after
 * injector creation.
 *
 * @author Vyacheslav Rusakov
 * @since 01.09.2014
 */
public class FeaturesHolder {
    private final List<FeatureInstaller> installers;
    private final Map<FeatureInstaller, List<Class>> features = Maps.newHashMap();

    public FeaturesHolder(final List<FeatureInstaller> installers) {
        this.installers = installers;
    }

    /**
     * @param installer installer instance
     * @param feature   feature type to store
     */
    public void register(final FeatureInstaller installer, final Class feature) {
        Preconditions.checkArgument(installers.contains(installer), "Installer %s not registered",
                installer.getClass().getSimpleName());
        if (!features.containsKey(installer)) {
            features.put(installer, Lists.<Class>newArrayList());
        }
        features.get(installer).add(feature);
    }

    /**
     * @return list of all registered installer instances
     */
    public List<FeatureInstaller> getInstallers() {
        return installers;
    }

    /**
     * @param installer installer instance
     * @return list of all found extensions for installer or null if nothing found.
     */
    public List<Class> getFeatures(final FeatureInstaller installer) {
        return features.get(installer);
    }
}
