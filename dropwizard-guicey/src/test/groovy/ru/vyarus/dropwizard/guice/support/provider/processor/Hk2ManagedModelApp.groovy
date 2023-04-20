package ru.vyarus.dropwizard.guice.support.provider.processor

import io.dropwizard.Application
import io.dropwizard.Configuration
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle

/**
 * @author Vyacheslav Rusakov
 * @since 05.12.2022
 */
class Hk2ManagedModelApp extends Application<Configuration> {

    @Override
    void initialize(Bootstrap<Configuration> bootstrap) {
        bootstrap.addBundle(GuiceBundle.builder()
                .extensions(Hk2ManagedProcessor)
                .build())
    }

    @Override
    void run(Configuration configuration, Environment environment) throws Exception {

    }
}
