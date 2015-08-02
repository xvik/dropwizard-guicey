package ru.vyarus.dropwizard.guice.bundles.dwbundle

import io.dropwizard.Bundle
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import ru.vyarus.dropwizard.guice.module.installer.feature.LifeCycleInstaller
import ru.vyarus.dropwizard.guice.module.installer.feature.ManagedInstaller
import ru.vyarus.dropwizard.guice.module.installer.feature.jersey.provider.JerseyProviderInstaller
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBootstrap
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBundle

/**
 * @author Vyacheslav Rusakov 
 * @since 02.08.2015
 */
class Bundle1 implements Bundle, GuiceyBundle {

    @Override
    void initialize(Bootstrap<?> bootstrap) {
    }

    @Override
    void initialize(GuiceyBootstrap bootstrap) {
        bootstrap.installers(
                LifeCycleInstaller.class,
                ManagedInstaller.class,
                JerseyProviderInstaller.class
        )
    }

    @Override
    void run(Environment environment) {
    }
}
