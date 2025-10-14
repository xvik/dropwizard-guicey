package ru.vyarus.dropwizard.guice.test.rest.support;

import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.UriInfo;

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

    @PATCH
    @Path("/{foo}")
    public String patch(@PathParam("foo") String foo) {
        throw new IllegalStateException("error");
    }

    @DELETE
    @Path("/{foo}")
    public void delete(@PathParam("foo") String foo) {
        throw new IllegalStateException("error");
    }
}
