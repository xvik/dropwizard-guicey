package ru.vyarus.dropwizard.guice.config.support

import io.dropwizard.core.Application
import io.dropwizard.core.setup.Bootstrap
import io.dropwizard.core.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.config.support.conf.ConfigLevel2

/**
 * @author Vyacheslav Rusakov
 * @since 20.06.2016
 */
class NoIfaceBindingApp extends Application<ConfigLevel2> {

    @Override
    void initialize(Bootstrap<ConfigLevel2> bootstrap) {
        bootstrap.addBundle(GuiceBundle.builder()
                .build())
    }

    @Override
    void run(ConfigLevel2 configuration, Environment environment) throws Exception {
    }
}
