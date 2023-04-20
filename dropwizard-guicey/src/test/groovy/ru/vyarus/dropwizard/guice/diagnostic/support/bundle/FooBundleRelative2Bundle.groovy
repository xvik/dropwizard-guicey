package ru.vyarus.dropwizard.guice.diagnostic.support.bundle

import ru.vyarus.dropwizard.guice.diagnostic.support.features.FooModule
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBootstrap
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBundle

/**
 * @author Vyacheslav Rusakov
 * @since 02.08.2016
 */
class FooBundleRelative2Bundle implements GuiceyBundle {

    @Override
    void initialize(GuiceyBootstrap bootstrap) {
        bootstrap.modules(new FooModule())
    }
}
