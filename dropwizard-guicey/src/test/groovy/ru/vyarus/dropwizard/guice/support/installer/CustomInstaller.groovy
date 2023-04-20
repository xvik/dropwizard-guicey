package ru.vyarus.dropwizard.guice.support.installer

import io.dropwizard.setup.Environment
import ru.vyarus.dropwizard.guice.module.installer.FeatureInstaller
import ru.vyarus.dropwizard.guice.module.installer.install.InstanceInstaller
import ru.vyarus.dropwizard.guice.module.installer.util.FeatureUtils
import ru.vyarus.dropwizard.guice.support.feature.CustomFeature

/**
 * Custom installer creation demo.
 *
 * @author Vyacheslav Rusakov 
 * @since 04.09.2014
 */
class CustomInstaller implements FeatureInstaller, InstanceInstaller<CustomFeature> {

    public static CustomFeature feature

    @Override
    boolean matches(Class<?> type) {
        return FeatureUtils.is(type, CustomFeature)
    }

    @Override
    void install(Environment environment, CustomFeature instance) {
        feature = instance
    }

    @Override
    void report() {
    }
}
