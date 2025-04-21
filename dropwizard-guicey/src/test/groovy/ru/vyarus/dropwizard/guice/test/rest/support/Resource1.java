package ru.vyarus.dropwizard.guice.test.rest.support;

import com.google.common.base.Preconditions;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
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
@Path("/1/")
@Produces("application/json")
public class Resource1 {

    @GET
    @Path("/{foo}")
    public String get(@PathParam("foo") String foo, @Context UriInfo uriInfo) {
        Preconditions.checkNotNull(uriInfo);
        return foo;
    }

    @POST
    @Path("/{foo}")
    public void post(@PathParam("foo") String foo) {
    }

    @PUT
    @Path("/{foo}")
    public String put(@PathParam("foo") String foo) {
        return foo;
    }

    @DELETE
    @Path("/{foo}")
    public void delete(@PathParam("foo") String foo) {
    }
}
