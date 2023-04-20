package ru.vyarus.dropwizard.guice.cases.multicases.support

import ru.vyarus.dropwizard.guice.module.installer.FeatureInstaller

/**
 * @author Vyacheslav Rusakov
 * @since 01.08.2016
 */
class CustomInstaller implements FeatureInstaller {

    @Override
    boolean matches(Class type) {
        return false
    }

    @Override
    void report() {

    }
}
