package ru.vyarus.dropwizard.guice.support

import io.dropwizard.core.Application
import io.dropwizard.core.setup.Bootstrap
import io.dropwizard.core.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.module.installer.feature.TaskInstaller

/**
 * Example of disabling installers and commands auto search not active.
 *
 * @author Vyacheslav Rusakov 
 * @since 04.09.2014
 */
class DisabledFeatureApplication extends Application<TestConfiguration> {

    public static void main(String[] args) {
        new DisabledFeatureApplication().run(args)
    }

    @Override
    void initialize(Bootstrap<TestConfiguration> bootstrap) {
        bootstrap.addBundle(GuiceBundle.builder()
                .enableAutoConfig("ru.vyarus.dropwizard.guice.support.feature")
                .disableInstallers(TaskInstaller)
                .build()
        );
    }

    @Override
    void run(TestConfiguration configuration, Environment environment) throws Exception {
    }
}