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
import ru.vyarus.dropwizard.guice.support.provider.oauth.OAuthDynamicFeature
import ru.vyarus.dropwizard.guice.support.util.BindModule

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
                .noDefaultInstallers()
                .installers(JerseyProviderInstaller, ResourceInstaller)
                .extensions(
                LocaleInjectableProvider,
                CustomFeatureInjectableProvider,
                InjectableProviderTestResource,
                AuthFactory,
                AuthFactoryProvider,
                AuthInjectionResolver)
                .modules(new BindModule(AuthFactoryProvider,
                OAuthDynamicFeature.OAuthAuthenticator,
                OAuthDynamicFeature.OAuthAuthorizer))
                .build()
        );
    }

    @Override
    void run(TestConfiguration configuration, Environment environment) throws Exception {
    }
}
