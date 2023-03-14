package ru.vyarus.dropwizard.guice.support.web

import com.google.inject.Binder
import com.google.inject.Module
import io.dropwizard.core.Application
import io.dropwizard.core.setup.Bootstrap
import io.dropwizard.core.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.support.TestConfiguration
import ru.vyarus.dropwizard.guice.support.feature.DummyService

/**
 * @author Vyacheslav Rusakov 
 * @since 12.10.2014
 */
class ServletsApplication extends Application<TestConfiguration> {

    @Override
    void initialize(Bootstrap<TestConfiguration> bootstrap) {
        bootstrap.addBundle(GuiceBundle.builder()
                .enableAutoConfig("ru.vyarus.dropwizard.guice.support.web.feature")
                .modules(new WebModule(), new Module() {
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
