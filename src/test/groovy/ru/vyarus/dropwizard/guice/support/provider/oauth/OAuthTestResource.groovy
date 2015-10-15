package ru.vyarus.dropwizard.guice.support.provider.oauth

import com.google.common.base.Preconditions
import io.dropwizard.auth.Auth

import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.Response

/**
 * @author Vyacheslav Rusakov 
 * @since 09.10.2014
 */
@Path("/prototype")
@Produces("application/json")
@javax.inject.Singleton
class OAuthTestResource {

    @GET
    @Path("/")
    public Response latest(@Auth User user) {
        Preconditions.checkNotNull(user)
        Preconditions.checkArgument(user.name == "valid")
        return Response.ok().build();
    }
}
