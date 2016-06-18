package ru.vyarus.dropwizard.guice.support.util

import com.google.inject.AbstractModule
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBootstrap
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBundle

/**
 * Applies guice restrictions for tests to make sure guicey is able to work in such conditions.
 *
 * @author Vyacheslav Rusakov
 * @since 19.06.2016
 */
class GuiceRestrictedConfigBundle implements GuiceyBundle {

    @Override
    void initialize(GuiceyBootstrap bootstrap) {
        bootstrap.modules(new AbstractModule() {
            @Override
            protected void configure() {
                binder().disableCircularProxies()
                binder().requireExactBindingAnnotations()
                binder().requireExplicitBindings()
            }
        })
    }
}
