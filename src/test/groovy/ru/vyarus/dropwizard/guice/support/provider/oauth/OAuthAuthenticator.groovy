package ru.vyarus.dropwizard.guice.support.provider.oauth

import com.google.common.base.Optional
import io.dropwizard.auth.AuthenticationException
import io.dropwizard.auth.Authenticator

import javax.inject.Singleton

/**
 * @author Vyacheslav Rusakov 
 * @since 14.10.2015
 */
@Singleton
class OAuthAuthenticator implements Authenticator<String, User> {

    @Override
    Optional<User> authenticate(String credentials) throws AuthenticationException {
        return Optional.fromNullable(credentials == "valid" ? new User(name: "valid") : null)
    }
}
