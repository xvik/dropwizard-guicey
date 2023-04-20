package ru.vyarus.dropwizard.guice.examples.auth;

import io.dropwizard.auth.AuthDynamicFeature;
import io.dropwizard.auth.AuthValueFactoryProvider;
import io.dropwizard.auth.oauth.OAuthCredentialAuthFilter;
import io.dropwizard.setup.Environment;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.ext.Provider;

/**
 * Configure OAuth authentication, almost the same way as described in dropwizard guide.
 * Note that authorizer and authenticator are guice beans (they will be initialized by guice on first injection
 * request).
 *
 * @author Vyacheslav Rusakov
 * @since 25.01.2019
 */
@Singleton
@Provider
// will be installed by JerseyProviderInstaller
public class OAuthDynamicFeature extends AuthDynamicFeature {

    @Inject
    public OAuthDynamicFeature(UserAuthenticator authenticator,
                               UserAuthorizer authorizer,
                               Environment environment) {
        super(new OAuthCredentialAuthFilter.Builder<User>()
                .setAuthenticator(authenticator)
                .setAuthorizer(authorizer)
                .setPrefix("Bearer")
                .buildAuthFilter());

        environment.jersey().register(RolesAllowedDynamicFeature.class);
        environment.jersey().register(new AuthValueFactoryProvider.Binder<User>(User.class));
    }

}