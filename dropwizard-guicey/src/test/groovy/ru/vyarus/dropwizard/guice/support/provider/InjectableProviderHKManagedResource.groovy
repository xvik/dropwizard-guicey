package ru.vyarus.dropwizard.guice.support.provider

import com.google.common.base.Preconditions
import ru.vyarus.dropwizard.guice.module.installer.feature.jersey.JerseyManaged
import ru.vyarus.dropwizard.guice.support.feature.CustomFeature
import ru.vyarus.dropwizard.guice.support.provider.annotated.Auth
import ru.vyarus.dropwizard.guice.support.provider.annotated.User

import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.Context
import jakarta.ws.rs.core.Response

/**
 * @author Vyacheslav Rusakov 
 * @since 25.11.2014
 */
@Path("/prototype")
@Produces("application/json")
@JerseyManaged
@jakarta.inject.Singleton
class InjectableProviderHKManagedResource {

    @GET
    @Path("/")
    public Response latest(@Context Locale locale, @Context CustomFeature feature, @Auth User user) {
        Preconditions.checkNotNull(locale)
        Preconditions.checkNotNull(feature)
        Preconditions.checkNotNull(user)
        return Response.ok().build();
    }
}

