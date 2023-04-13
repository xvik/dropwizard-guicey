package ru.vyarus.dropwizard.guice.support.provider

import com.google.common.base.Preconditions
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
 * @since 09.10.2014
 */
@Path("/prototype")
@Produces("application/json")
@jakarta.inject.Singleton
class InjectableProviderTestResource {

    @GET
    @Path("/")
    public Response latest(@Context Locale locale, @Context CustomFeature feature, @Auth User user) {
        Preconditions.checkNotNull(locale)
        Preconditions.checkNotNull(feature)
        Preconditions.checkNotNull(user)
        return Response.ok().build();
    }
}
