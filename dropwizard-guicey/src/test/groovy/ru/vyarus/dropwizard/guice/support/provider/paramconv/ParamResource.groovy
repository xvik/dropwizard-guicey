package ru.vyarus.dropwizard.guice.support.provider.paramconv

import ru.vyarus.dropwizard.guice.support.feature.CustomFeature
import ru.vyarus.dropwizard.guice.support.provider.annotated.Auth
import ru.vyarus.dropwizard.guice.support.provider.annotated.User

import jakarta.inject.Singleton
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.Context
import jakarta.ws.rs.core.Response

/**
 * @author Vyacheslav Rusakov 
 * @since 03.09.2015
 */
@Path("/param")
@Produces("application/json")
@Singleton
class ParamResource {

    @GET
    @Path("/{foo}")
    public Response foo(@PathParam("foo") Foo foo) {
        return Response.ok(foo).build()
    }
}
