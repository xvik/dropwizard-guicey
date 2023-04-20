package ru.vyarus.dropwizard.guice.config.support

import io.dropwizard.Application
import io.dropwizard.Configuration
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle

/**
 * @author Vyacheslav Rusakov
 * @since 19.06.2016
 */
class BasicApplication extends Application<Configuration> {

    @Override
    void initialize(Bootstrap<Configuration> bootstrap) {
        bootstrap.addBundle(GuiceBundle.builder()
                .build())
    }

    @Override
    void run(Configuration configuration, Environment environment) throws Exception {
    }
}
