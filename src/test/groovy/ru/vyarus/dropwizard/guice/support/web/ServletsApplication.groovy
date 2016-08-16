package ru.vyarus.dropwizard.guice.support.web

import io.dropwizard.Application
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.support.TestConfiguration
import ru.vyarus.dropwizard.guice.support.feature.DummyService
import ru.vyarus.dropwizard.guice.support.util.BindModule

/**
 * @author Vyacheslav Rusakov 
 * @since 12.10.2014
 */
class ServletsApplication extends Application<TestConfiguration> {

    @Override
    void initialize(Bootstrap<TestConfiguration> bootstrap) {
        bootstrap.addBundle(GuiceBundle.<TestConfiguration> builder()
                .enableAutoConfig("ru.vyarus.dropwizard.guice.support.web.feature")
                .useWebInstallers()
                .modules(new WebModule(), new BindModule(DummyService))
                .build()
        );
    }

    @Override
    void run(TestConfiguration configuration, Environment environment) throws Exception {
    }
}
