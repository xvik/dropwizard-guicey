package ru.vyarus.dropwizard.guice.diagnostic.support.bundle

import ru.vyarus.dropwizard.guice.diagnostic.support.module.OverridingModule
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBootstrap
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBundle

/**
 * @author Vyacheslav Rusakov
 * @since 11.10.2019
 */
class LookupBundle implements GuiceyBundle {

    @Override
    void initialize(GuiceyBootstrap bootstrap) {
        bootstrap.modulesOverride(new OverridingModule())
    }
}
