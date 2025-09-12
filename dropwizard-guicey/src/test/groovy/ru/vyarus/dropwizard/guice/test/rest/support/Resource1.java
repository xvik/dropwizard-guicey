package ru.vyarus.dropwizard.guice.test.rest.support;

import com.google.common.base.Preconditions;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PATCH;
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

    @PATCH
    @Path("/{foo}")
    public String patch(@PathParam("foo") String foo) {
        return foo;
    }

    @DELETE
    @Path("/{foo}")
    public void delete(@PathParam("foo") String foo) {
    }
}
