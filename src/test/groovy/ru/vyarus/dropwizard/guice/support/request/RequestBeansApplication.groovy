package ru.vyarus.dropwizard.guice.support.request

import io.dropwizard.Application
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.support.TestConfiguration
import ru.vyarus.dropwizard.guice.support.util.BindModule

/**
 * Application used to validate request scoped beans.
 *
 * @author Vyacheslav Rusakov
 * @since 04.10.2014
 */
class RequestBeansApplication extends Application<TestConfiguration> {

    public static void main(String[] args) {
        new RequestBeansApplication().run(args)
    }

    @Override
    void initialize(Bootstrap<TestConfiguration> bootstrap) {
        bootstrap.addBundle(GuiceBundle.<TestConfiguration> builder()
                .enableAutoConfig("ru.vyarus.dropwizard.guice.support.request")
                .modules(new BindModule(RequestScopedBean))
                .build()
        );
    }

    @Override
    void run(TestConfiguration configuration, Environment environment) throws Exception {
    }
}
