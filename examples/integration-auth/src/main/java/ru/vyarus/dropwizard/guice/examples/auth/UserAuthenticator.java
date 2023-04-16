package ru.vyarus.dropwizard.guice.examples.auth;

import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;

import jakarta.inject.Singleton;
import java.util.Optional;

/**
 * Validate credentials and provide user object.
 * Guice bean.
 *
 * @author Vyacheslav Rusakov
 * @since 25.01.2019
 */
@Singleton
public class UserAuthenticator implements Authenticator<String, User> {

    @Override
    public Optional<User> authenticate(String credentials) throws AuthenticationException {
        return Optional.ofNullable("valid".equals(credentials) ? new User("admin", "ADMIN") : null);
    }
}