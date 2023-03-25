package ru.vyarus.dropwizard.guice.support.resource

import com.google.inject.Inject
import ru.vyarus.dropwizard.guice.support.feature.DummyService

import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.Response

/**
 * Single resource instance will be used for all requests.
 *
 * @author Vyacheslav Rusakov 
 * @since 04.10.2014
 */
@Path("/singleton")
@Produces('application/json')
@javax.inject.Singleton
class SingletonResource {
    static int creationCounter = 0
    static int callCounter = 0

    @Inject
    DummyService service

    static void reset(){
        creationCounter = 0
        callCounter = 0
    }

    SingletonResource() {
        creationCounter++
    }

    @GET
    @Path("/")
    public Response latest() {
        callCounter++
        return Response.ok().build();
    }
}
