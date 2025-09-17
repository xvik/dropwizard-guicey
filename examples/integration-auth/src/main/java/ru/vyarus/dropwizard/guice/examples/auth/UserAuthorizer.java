package ru.vyarus.dropwizard.guice.examples.auth;

import io.dropwizard.auth.Authorizer;
import jakarta.inject.Singleton;
import jakarta.ws.rs.container.ContainerRequestContext;
import org.jspecify.annotations.Nullable;

/**
 * Checks if authorized user has required role ({@link jakarta.annotation.security.RolesAllowed} annotation support).
 * Guice bean.
 *
 * @author Vyacheslav Rusakov
 * @since 25.01.2019
 */
@Singleton
public class UserAuthorizer implements Authorizer<User> {
    @Override
    public boolean authorize(User user, String role, @Nullable ContainerRequestContext requestContext) {
        return user.getRoles().contains(role);
    }
}