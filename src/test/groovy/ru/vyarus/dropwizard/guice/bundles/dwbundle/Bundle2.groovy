package ru.vyarus.dropwizard.guice.bundles.dwbundle

import io.dropwizard.ConfiguredBundle
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import ru.vyarus.dropwizard.guice.module.installer.feature.eager.EagerSingletonInstaller
import ru.vyarus.dropwizard.guice.module.installer.feature.health.HealthCheckInstaller
import ru.vyarus.dropwizard.guice.module.installer.feature.jersey.ResourceInstaller
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBootstrap
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBundle
import ru.vyarus.dropwizard.guice.support.TestConfiguration

/**
 * @author Vyacheslav Rusakov 
 * @since 02.08.2015
 */
class Bundle2 implements ConfiguredBundle<TestConfiguration>, GuiceyBundle {

    @Override
    void run(TestConfiguration configuration, Environment environment) throws Exception {
    }

    @Override
    void initialize(Bootstrap<?> bootstrap) {
    }


    @Override
    void initialize(GuiceyBootstrap bootstrap) {
        bootstrap.installers(
                ResourceInstaller.class,
                EagerSingletonInstaller.class,
                HealthCheckInstaller.class
        )
    }
}
