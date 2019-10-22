package ru.vyarus.dropwizard.guice.support.order

import com.google.inject.Binder
import com.google.inject.Module
import io.dropwizard.Application
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.support.TestConfiguration
import ru.vyarus.dropwizard.guice.support.feature.DummyResource
import ru.vyarus.dropwizard.guice.support.feature.DummyService

/**
 * Application to check extension ordering.
 *
 * @author Vyacheslav Rusakov 
 * @since 13.10.2014
 */
class OrderedApplication extends Application<TestConfiguration> {

    @Override
    void initialize(Bootstrap<TestConfiguration> bootstrap) {
        bootstrap.addBundle(GuiceBundle.builder()
                .enableAutoConfig("ru.vyarus.dropwizard.guice.support.order")
                // at least one resource required
                .extensions(DummyResource)
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
