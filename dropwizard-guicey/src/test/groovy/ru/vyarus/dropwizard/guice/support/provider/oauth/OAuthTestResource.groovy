package ru.vyarus.dropwizard.guice.support.provider.oauth

import com.google.common.base.Preconditions
import io.dropwizard.auth.Auth

import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.Response

/**
 * @author Vyacheslav Rusakov 
 * @since 09.10.2014
 */
@Path("/prototype")
@Produces("application/json")
@jakarta.inject.Singleton
class OAuthTestResource {

    @GET
    @Path("/")
    public Response latest(@Auth User user) {
        Preconditions.checkNotNull(user)
        Preconditions.checkArgument(user.name == "valid")
        return Response.ok().build();
    }
}
