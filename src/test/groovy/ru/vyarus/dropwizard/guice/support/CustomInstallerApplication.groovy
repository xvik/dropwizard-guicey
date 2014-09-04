package ru.vyarus.dropwizard.guice.support

import io.dropwizard.Application
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle

/**
 * @author Vyacheslav Rusakov 
 * @since 04.09.2014
 */
class CustomInstallerApplication extends Application<TestConfiguration> {

    public static void main(String[] args) {
        new CustomInstallerApplication().run(args)
    }

    @Override
    void initialize(Bootstrap<TestConfiguration> bootstrap) {
        bootstrap.addBundle(GuiceBundle.<TestConfiguration> builder()
                .enableAutoConfig(
                "ru.vyarus.dropwizard.guice.support.feature",
                "ru.vyarus.dropwizard.guice.support.installer")
                .build()
        );
    }

    @Override
    void run(TestConfiguration configuration, Environment environment) throws Exception {
    }
}
