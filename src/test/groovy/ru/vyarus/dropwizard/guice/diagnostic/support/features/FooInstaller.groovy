package ru.vyarus.dropwizard.guice.diagnostic.support.features

import ru.vyarus.dropwizard.guice.module.installer.FeatureInstaller

/**
 * @author Vyacheslav Rusakov
 * @since 26.06.2016
 */
class FooInstaller implements FeatureInstaller<Object> {

    @Override
    boolean matches(Class<?> type) {
        return false
    }

    @Override
    void report() {
    }
}
