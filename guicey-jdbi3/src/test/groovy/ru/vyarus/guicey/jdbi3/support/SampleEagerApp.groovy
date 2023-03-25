package ru.vyarus.guicey.jdbi3.support

import io.dropwizard.core.Application
import io.dropwizard.core.setup.Bootstrap
import io.dropwizard.core.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.guicey.jdbi3.JdbiBundle

/**
 * @author Vyacheslav Rusakov
 * @since 23.06.2020
 */
class SampleEagerApp extends Application<SampleConfiguration> {

    @Override
    void initialize(Bootstrap<SampleConfiguration> bootstrap) {
        bootstrap.addBundle(GuiceBundle.builder()
                .enableAutoConfig(SampleApp.package.name)
                .bundles(JdbiBundle.<SampleConfiguration> forDatabase { conf, env -> conf.database }
                        .withEagerInitialization())
                .build())
    }

    @Override
    void run(SampleConfiguration configuration, Environment environment) throws Exception {
    }
}
