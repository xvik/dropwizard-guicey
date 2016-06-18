package ru.vyarus.dropwizard.guice.support.provider.oauth

import io.dropwizard.Application
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.module.installer.feature.jersey.ResourceInstaller
import ru.vyarus.dropwizard.guice.module.installer.feature.jersey.provider.JerseyProviderInstaller
import ru.vyarus.dropwizard.guice.support.TestConfiguration
import ru.vyarus.dropwizard.guice.support.util.BindModule

/**
 * @author Vyacheslav Rusakov 
 * @since 14.10.2015
 */
class OauthCheckApplication extends Application<TestConfiguration> {

    @Override
    void initialize(Bootstrap<TestConfiguration> bootstrap) {
        bootstrap.addBundle(GuiceBundle.<TestConfiguration> builder()
                .installers(ResourceInstaller, JerseyProviderInstaller)
                .extensions(OAuthTestResource, OAuthDynamicFeature)
                .modules(new BindModule(OAuthDynamicFeature.OAuthAuthenticator,
                OAuthDynamicFeature.OAuthAuthorizer))
                .build()
        );
    }

    @Override
    void run(TestConfiguration configuration, Environment environment) throws Exception {
    }
}
