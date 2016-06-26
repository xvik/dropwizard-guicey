package ru.vyarus.dropwizard.guice.bundles.transitive.support

import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBootstrap
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBundle

/**
 * @author Vyacheslav Rusakov
 * @since 26.06.2016
 */
class Bundle1 implements GuiceyBundle {

    static boolean called

    @Override
    void initialize(GuiceyBootstrap bootstrap) {
        bootstrap.bundles(new Bundle2())
        called = true
    }
}
