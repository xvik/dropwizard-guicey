package ru.vyarus.dropwizard.guice.support.installerorder

import io.dropwizard.Application
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.support.TestConfiguration
import ru.vyarus.dropwizard.guice.support.feature.DummyResource

/**
 * Application to check installers ordering.
 *
 * @author Vyacheslav Rusakov 
 * @since 17.04.2015
 */
class OrderedInstallersApplication extends Application<TestConfiguration>{

    @Override
    void initialize(Bootstrap<TestConfiguration> bootstrap) {
        bootstrap.addBundle(GuiceBundle.<TestConfiguration> builder()
                .enableAutoConfig("ru.vyarus.dropwizard.guice.support.installerorder")
                // at least one resource required
                .extensions(DummyResource)
                .build()
        );
    }

    @Override
    void run(TestConfiguration configuration, Environment environment) throws Exception {
    }
}
