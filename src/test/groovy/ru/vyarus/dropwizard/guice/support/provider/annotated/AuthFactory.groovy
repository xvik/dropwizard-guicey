package ru.vyarus.dropwizard.guice.support.provider.annotated

import org.glassfish.hk2.api.Factory

import javax.ws.rs.ext.Provider

/**
 * @author Vyacheslav Rusakov 
 * @since 20.11.2014
 */
@Provider
class AuthFactory implements Factory<User>{

    @Override
    User provide() {
        return new User()
    }

    @Override
    void dispose(User instance) {
    }
}
