package ru.vyarus.dropwizard.guice.support.provider.annotatedhkmanaged


import org.glassfish.jersey.server.ContainerRequest
import ru.vyarus.dropwizard.guice.module.installer.feature.jersey.HK2Managed
import ru.vyarus.dropwizard.guice.support.provider.annotated.User

import javax.ws.rs.ext.Provider
import java.util.function.Function

/**
 * @author Vyacheslav Rusakov 
 * @since 20.11.2014
 */
@Provider
@HK2Managed
class AuthFactoryHK implements Function<ContainerRequest, User> {

    @Override
    User apply(ContainerRequest containerRequest) {
        return new User()
    }
}
