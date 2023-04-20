package ru.vyarus.guicey.jdbi.support

import io.dropwizard.Application
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.guicey.jdbi.JdbiBundle

/**
 * @author Vyacheslav Rusakov
 * @since 05.12.2016
 */
class SampleApp extends Application<SampleConfiguration> {

    @Override
    void initialize(Bootstrap<SampleConfiguration> bootstrap) {
        bootstrap.addBundle(GuiceBundle.builder()
                .enableAutoConfig(SampleApp.package.name)
                .bundles(JdbiBundle.<SampleConfiguration> forDatabase { conf, env -> conf.database })
                .build())
    }

    @Override
    void run(SampleConfiguration configuration, Environment environment) throws Exception {
    }
}
