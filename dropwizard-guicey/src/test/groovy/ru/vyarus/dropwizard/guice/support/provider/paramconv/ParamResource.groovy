package ru.vyarus.dropwizard.guice.support.provider.paramconv


import javax.inject.Singleton
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.core.Response

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
