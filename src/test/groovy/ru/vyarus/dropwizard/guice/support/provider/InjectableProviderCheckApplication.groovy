package ru.vyarus.dropwizard.guice.support.provider

import io.dropwizard.Application
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.module.installer.feature.JerseyInjectableProviderInstaller
import ru.vyarus.dropwizard.guice.module.installer.feature.ResourceInstaller
import ru.vyarus.dropwizard.guice.support.TestConfiguration

/**
 * Injectable providers are always registered as singletons no matter what getScope method returns.
 *
 * @author Vyacheslav Rusakov 
 * @since 09.10.2014
 */
class InjectableProviderCheckApplication extends Application<TestConfiguration> {

    @Override
    void initialize(Bootstrap<TestConfiguration> bootstrap) {
        bootstrap.addBundle(GuiceBundle.<TestConfiguration> builder()
                .features(JerseyInjectableProviderInstaller, ResourceInstaller)
                .beans(LocaleInjectableProvider, CustomFeatureInjectableProvider, InjectableProviderTestResource)
                .build()
        );
    }

    @Override
    void run(TestConfiguration configuration, Environment environment) throws Exception {
    }
}
