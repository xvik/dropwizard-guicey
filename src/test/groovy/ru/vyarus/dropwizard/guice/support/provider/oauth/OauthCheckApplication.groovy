package ru.vyarus.dropwizard.guice.support.provider.oauth

import io.dropwizard.Application
import io.dropwizard.auth.AuthFactory
import io.dropwizard.auth.oauth.OAuthFactory
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.injector.lookup.InjectorLookup
import ru.vyarus.dropwizard.guice.module.installer.feature.jersey.ResourceInstaller
import ru.vyarus.dropwizard.guice.support.TestConfiguration

/**
 * @author Vyacheslav Rusakov 
 * @since 14.10.2015
 */
class OauthCheckApplication extends Application<TestConfiguration> {

    @Override
    void initialize(Bootstrap<TestConfiguration> bootstrap) {
        bootstrap.addBundle(GuiceBundle.<TestConfiguration> builder()
                .installers(ResourceInstaller)
                .extensions(OAuthTestResource)
                .build()
        );
    }

    @Override
    void run(TestConfiguration configuration, Environment environment) throws Exception {
        environment.jersey().register(AuthFactory
                .binder(new OAuthFactory<User>(
                InjectorLookup.getInjector(this).get().getInstance(OAuthAuthenticator),
                "SUPER SECRET STUFF",
                User.class)));
    }
}
