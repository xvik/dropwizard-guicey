package ru.vyarus.dropwizard.guice.support.provider.annotated


import org.glassfish.jersey.server.ContainerRequest

import javax.ws.rs.ext.Provider
import java.util.function.Function

/**
 * @author Vyacheslav Rusakov 
 * @since 20.11.2014
 */
@Provider
class AuthFactory implements Function<ContainerRequest, User> {

    @Override
    User apply(ContainerRequest containerRequest) {
        return new User()
    }
}
