package ru.vyarus.dropwizard.guice.support.resource

import com.google.inject.Inject
import ru.vyarus.dropwizard.guice.support.feature.DummyService

import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.Response

/**
 * Resource will be instantiated for evert request.
 *
 * @author Vyacheslav Rusakov 
 * @since 04.10.2014
 */
@Path("/prototype")
@Produces('application/json')
class PrototypeResource {
    static int creationCounter = 0
    static int callCounter = 0

    @Inject
    DummyService service

    static void reset(){
        creationCounter = 0
        callCounter = 0
    }

    PrototypeResource() {
        creationCounter++
    }

    @GET
    @Path("/")
    public Response latest() {
        callCounter++
        return Response.ok().build();
    }
}
