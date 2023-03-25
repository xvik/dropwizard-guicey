package ru.vyarus.dropwizard.guice.bundles.transitive.support.loop2

import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBootstrap
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBundle

/**
 * @author Vyacheslav Rusakov
 * @since 26.06.2016
 */
class LoopBundle1 implements GuiceyBundle {

    @Override
    void initialize(GuiceyBootstrap bootstrap) {
        bootstrap.bundles(new LoopBundle2())
    }
}
