package ru.vyarus.dropwizard.guice.support.feature

import com.google.inject.Inject

import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.Response

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
