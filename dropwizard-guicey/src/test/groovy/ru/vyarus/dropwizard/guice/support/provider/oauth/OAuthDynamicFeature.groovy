package ru.vyarus.dropwizard.guice.support.provider.oauth

import io.dropwizard.auth.*
import io.dropwizard.auth.oauth.OAuthCredentialAuthFilter
import io.dropwizard.core.setup.Environment
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature

import javax.inject.Inject
import javax.inject.Singleton
import javax.ws.rs.container.ContainerRequestContext
import javax.ws.rs.core.Feature
import javax.ws.rs.core.FeatureContext
import javax.ws.rs.ext.Provider

/**
 * @author Vyacheslav Rusakov
 * @since 13.01.2016
 */
@Singleton
@Provider
// will be installed by JerseyProviderInstaller
class OAuthDynamicFeature extends AuthDynamicFeature {

    @Inject
    OAuthDynamicFeature(OAuthAuthenticator authenticator, OAuthAuthorizer authorizer, Environment environment) {
        super(new OAuthCredentialAuthFilter.Builder<User>()
                .setAuthenticator(authenticator)
                .setAuthorizer(authorizer)
                .setPrefix("Bearer")
                .buildAuthFilter())

        environment.jersey().register(RolesAllowedDynamicFeature.class)
        environment.jersey().register(new AuthValueFactoryProvider.Binder(User.class))
    }

    // may be external class (internal for simplicity)
    @Singleton
    static class OAuthAuthenticator implements Authenticator<String, User> {

        @Override
        Optional<User> authenticate(String credentials) throws AuthenticationException {
            return Optional.ofNullable(credentials == "valid" ? new User(name: "valid") : null)
        }
    }

    // may be external class (internal for simplicity)
    @Singleton
    static class OAuthAuthorizer implements Authorizer<User> {

        @Override
        boolean authorize(User user, String role, ContainerRequestContext requestContext) {
            return user.getName().equals("good-guy") && role.equals("ADMIN");
        }
    }

    // will be installed by JerseyFeatureInstaller
    static class ConfigurationFeature implements Feature {
        @Override
        boolean configure(FeatureContext context) {
            context.register(RolesAllowedDynamicFeature.class)
            context.register(new AuthValueFactoryProvider.Binder(User.class))
            return true;
        }
    }
}
