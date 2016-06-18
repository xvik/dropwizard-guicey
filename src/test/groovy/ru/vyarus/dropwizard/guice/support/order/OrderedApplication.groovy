package ru.vyarus.dropwizard.guice.support.order

import io.dropwizard.Application
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.support.TestConfiguration
import ru.vyarus.dropwizard.guice.support.feature.DummyResource
import ru.vyarus.dropwizard.guice.support.feature.DummyService
import ru.vyarus.dropwizard.guice.support.util.BindModule

/**
 * Application to check extension ordering.
 *
 * @author Vyacheslav Rusakov 
 * @since 13.10.2014
 */
class OrderedApplication extends Application<TestConfiguration> {

    @Override
    void initialize(Bootstrap<TestConfiguration> bootstrap) {
        bootstrap.addBundle(GuiceBundle.<TestConfiguration> builder()
                .enableAutoConfig("ru.vyarus.dropwizard.guice.support.order")
                // at least one resource required
                .extensions(DummyResource)
                .modules(new BindModule(DummyService))
                .build()
        );
    }

    @Override
    void run(TestConfiguration configuration, Environment environment) throws Exception {
    }
}
