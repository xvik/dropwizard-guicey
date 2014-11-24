package ru.vyarus.dropwizard.guice.support.provider.annotatedhkmanaged

import org.glassfish.hk2.api.Factory
import ru.vyarus.dropwizard.guice.module.installer.feature.jersey.HK2Managed
import ru.vyarus.dropwizard.guice.support.provider.annotated.User

import javax.ws.rs.ext.Provider

/**
 * @author Vyacheslav Rusakov 
 * @since 20.11.2014
 */
@Provider
@HK2Managed
class AuthFactoryHK implements Factory<User>{

    @Override
    User provide() {
        return new User()
    }

    @Override
    void dispose(User instance) {
    }
}
