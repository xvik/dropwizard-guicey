package ru.vyarus.guicey.admin.support

import io.dropwizard.core.Application
import io.dropwizard.core.Configuration
import io.dropwizard.core.setup.Bootstrap
import io.dropwizard.core.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.guicey.admin.AdminRestBundle

/**
 * @author Vyacheslav Rusakov 
 * @since 08.08.2015
 */
class AdminRestApplication extends Application<Configuration> {

    @Override
    void initialize(Bootstrap<Configuration> bootstrap) {
        bootstrap.addBundle(GuiceBundle.builder()
                .enableAutoConfig(getClass().package.name)
                .bundles(new AdminRestBundle())
                .build()
        );
    }

    @Override
    void run(Configuration configuration, Environment environment) throws Exception {
    }
}
