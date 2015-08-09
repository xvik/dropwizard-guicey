package ru.vyarus.dropwizard.guice.admin.support

import io.dropwizard.Application
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.admin.AdminRestBundle
import ru.vyarus.dropwizard.guice.support.TestConfiguration

/**
 * @author Vyacheslav Rusakov 
 * @since 08.08.2015
 */
class AdminRestApplication extends Application<TestConfiguration> {

    @Override
    void initialize(Bootstrap<TestConfiguration> bootstrap) {
        bootstrap.addBundle(new AdminRestBundle())
        bootstrap.addBundle(GuiceBundle.<TestConfiguration> builder()
                .enableAutoConfig(getClass().package.name)
                .build()
        );
    }

    @Override
    void run(TestConfiguration configuration, Environment environment) throws Exception {
    }
}
