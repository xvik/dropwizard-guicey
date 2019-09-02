package ru.vyarus.dropwizard.guice.support.resource

import com.google.inject.AbstractModule
import com.google.inject.Binder
import com.google.inject.Module
import io.dropwizard.Application
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.support.TestConfiguration
import ru.vyarus.dropwizard.guice.support.feature.DummyService

/**
 * @author Vyacheslav Rusakov 
 * @since 04.10.2014
 */
class ResourceSingletonCheckApplication extends Application<TestConfiguration> {

    @Override
    void initialize(Bootstrap<TestConfiguration> bootstrap) {
        bootstrap.addBundle(GuiceBundle.<TestConfiguration> builder()
                .enableAutoConfig("ru.vyarus.dropwizard.guice.support.resource")
                .modules(new Module() {
                    @Override
                    void configure(Binder binder) {
                        binder.bind(DummyService).asEagerSingleton()
                    }
                })
                .build()
        );
    }

    @Override
    void run(TestConfiguration configuration, Environment environment) throws Exception {
    }
}