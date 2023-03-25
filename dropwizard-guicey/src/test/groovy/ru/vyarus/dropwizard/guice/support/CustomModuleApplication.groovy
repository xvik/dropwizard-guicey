package ru.vyarus.dropwizard.guice.support

import io.dropwizard.core.Application
import io.dropwizard.core.setup.Bootstrap
import io.dropwizard.core.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle

/**
 *
 * @author Vyacheslav Rusakov 
 * @since 04.09.2014
 */
class CustomModuleApplication extends Application<TestConfiguration> {

    public static void main(String[] args) {
        new CustomInstallerApplication().run(args)
    }

    @Override
    void initialize(Bootstrap<TestConfiguration> bootstrap) {
        bootstrap.addBundle(GuiceBundle.builder()
                .enableAutoConfig("ru.vyarus.dropwizard.guice.support.feature")
                .searchCommands()
                .modules(new AutowiredModule())
                .build()
        );
    }

    @Override
    void run(TestConfiguration configuration, Environment environment) throws Exception {
    }
}
