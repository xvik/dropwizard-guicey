package ru.vyarus.dropwizard.guice.support.feature

import com.google.inject.Inject

import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.Response

/**
 * @author Vyacheslav Rusakov 
 * @since 01.09.2014
 */
@Path("/dummy")
@Produces('application/json')
class DummyResource {

    @Inject
    DummyService service

    @GET
    @Path("/")
    public Response latest() {
        return Response.ok().build();
    }
}
