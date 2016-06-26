package ru.vyarus.dropwizard.guice.bundles.transitive.support.loop

import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBootstrap
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBundle

/**
 * @author Vyacheslav Rusakov
 * @since 21.06.2016
 */
class LoopBundle implements GuiceyBundle {

    @Override
    void initialize(GuiceyBootstrap bootstrap) {
        // infinite transitive loop
        bootstrap.bundles(new LoopBundle())
    }
}
