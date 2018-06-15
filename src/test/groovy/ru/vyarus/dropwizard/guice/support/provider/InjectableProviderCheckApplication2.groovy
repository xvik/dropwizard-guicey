package ru.vyarus.dropwizard.guice.support.provider

import io.dropwizard.Application
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.module.installer.feature.jersey.ResourceInstaller
import ru.vyarus.dropwizard.guice.module.installer.feature.jersey.provider.JerseyProviderInstaller
import ru.vyarus.dropwizard.guice.support.TestConfiguration
import ru.vyarus.dropwizard.guice.support.provider.annotatedhkmanaged.AuthFactoryHK
import ru.vyarus.dropwizard.guice.support.provider.annotatedhkmanaged.AuthFactoryProviderHK
import ru.vyarus.dropwizard.guice.support.provider.annotatedhkmanaged.AuthInjectionResolverHK

/**
 * Use HK2 managed beans
 * @author Vyacheslav Rusakov 
 * @since 25.11.2014
 */
class InjectableProviderCheckApplication2 extends Application<TestConfiguration> {

    @Override
    void initialize(Bootstrap<TestConfiguration> bootstrap) {
        bootstrap.addBundle(GuiceBundle.<TestConfiguration> builder()
                .noDefaultInstallers()
                .installers(JerseyProviderInstaller, ResourceInstaller)
                .extensions(
                LocaleInjectableProvider,
                CustomFeatureInjectableProvider,
                InjectableProviderTestResource,
                AuthFactoryHK,
                AuthFactoryProviderHK,
                AuthInjectionResolverHK)
                .build()
        );
    }

    @Override
    void run(TestConfiguration configuration, Environment environment) throws Exception {
    }
}
