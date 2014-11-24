package ru.vyarus.dropwizard.guice.support.provider

import io.dropwizard.Application
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.module.installer.feature.jersey.ResourceInstaller
import ru.vyarus.dropwizard.guice.module.installer.feature.jersey.provider.JerseyProviderInstaller
import ru.vyarus.dropwizard.guice.support.TestConfiguration
import ru.vyarus.dropwizard.guice.support.provider.annotated.AuthFactory
import ru.vyarus.dropwizard.guice.support.provider.annotated.AuthFactoryProvider
import ru.vyarus.dropwizard.guice.support.provider.annotated.AuthInjectionResolver

/**
 * Only resource is HKmanaged now
 * @author Vyacheslav Rusakov 
 * @since 25.11.2014
 */
class InjectableProviderCheckApplication3 extends Application<TestConfiguration> {

    @Override
    void initialize(Bootstrap<TestConfiguration> bootstrap) {
        bootstrap.addBundle(GuiceBundle.<TestConfiguration> builder()
                .installers(JerseyProviderInstaller, ResourceInstaller)
                .extensions(
                LocaleInjectableProvider,
                CustomFeatureInjectableProvider,
                InjectableProviderHKManagedResource,
                AuthFactory,
                AuthFactoryProvider,
                AuthInjectionResolver)
                .build()
        );
    }

    @Override
    void run(TestConfiguration configuration, Environment environment) throws Exception {
    }
}
