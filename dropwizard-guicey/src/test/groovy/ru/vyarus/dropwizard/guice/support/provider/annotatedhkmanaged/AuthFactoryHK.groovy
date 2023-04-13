package ru.vyarus.dropwizard.guice.support.provider.annotatedhkmanaged


import org.glassfish.jersey.server.ContainerRequest
import ru.vyarus.dropwizard.guice.module.installer.feature.jersey.JerseyManaged
import ru.vyarus.dropwizard.guice.support.provider.annotated.User

import jakarta.ws.rs.ext.Provider
import java.util.function.Function

/**
 * @author Vyacheslav Rusakov 
 * @since 20.11.2014
 */
@Provider
@JerseyManaged
class AuthFactoryHK implements Function<ContainerRequest, User> {

    @Override
    User apply(ContainerRequest containerRequest) {
        return new User()
    }
}
