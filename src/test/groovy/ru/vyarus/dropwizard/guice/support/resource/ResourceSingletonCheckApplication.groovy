package ru.vyarus.dropwizard.guice.support.resource

import io.dropwizard.Application
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.support.TestConfiguration
import ru.vyarus.dropwizard.guice.support.feature.DummyService
import ru.vyarus.dropwizard.guice.support.util.BindModule

/**
 * @author Vyacheslav Rusakov 
 * @since 04.10.2014
 */
class ResourceSingletonCheckApplication extends Application<TestConfiguration> {

    @Override
    void initialize(Bootstrap<TestConfiguration> bootstrap) {
        bootstrap.addBundle(GuiceBundle.<TestConfiguration> builder()
                .enableAutoConfig("ru.vyarus.dropwizard.guice.support.resource")
                .modules(new BindModule(DummyService))
                .build()
        );
    }

    @Override
    void run(TestConfiguration configuration, Environment environment) throws Exception {
    }
}