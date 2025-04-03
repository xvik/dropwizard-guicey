package ru.vyarus.dropwizard.guice.test.jupiter.setup.rest.support;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

/**
 * @author Vyacheslav Rusakov
 * @since 22.02.2025
 */
@Path("/error/")
@Produces("application/json")
public class ErrorResource {

    @GET
    @Path("/{foo}")
    public String get(@PathParam("foo") String foo, @Context UriInfo uriInfo) {
        throw new IllegalStateException("error");
    }

    @POST
    @Path("/{foo}")
    public void post(@PathParam("foo") String foo) {
        throw new IllegalStateException("error");
    }

    @PUT
    @Path("/{foo}")
    public String put(@PathParam("foo") String foo) {
        throw new IllegalStateException("error");
    }

    @DELETE
    @Path("/{foo}")
    public void delete(@PathParam("foo") String foo) {
        throw new IllegalStateException("error");
    }
}
