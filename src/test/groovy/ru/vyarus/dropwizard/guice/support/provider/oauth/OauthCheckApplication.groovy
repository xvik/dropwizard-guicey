package ru.vyarus.dropwizard.guice.support.provider.oauth

import io.dropwizard.core.Application
import io.dropwizard.core.setup.Bootstrap
import io.dropwizard.core.setup.Environment
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
        bootstrap.addBundle(GuiceBundle.builder()
                .noDefaultInstallers()
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
