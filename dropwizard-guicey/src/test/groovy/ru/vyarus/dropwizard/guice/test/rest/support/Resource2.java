package ru.vyarus.dropwizard.guice.test.rest.support;

import com.google.common.base.Preconditions;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.UriInfo;

/**
 * @author Vyacheslav Rusakov
 * @since 22.02.2025
 */
@Path("/2/")
@Produces("application/json")
public class Resource2 {

    @GET
    @Path("/{foo}")
    public String get(@PathParam("foo") String foo, @Context UriInfo uriInfo) {
        Preconditions.checkNotNull(uriInfo);
        return foo;
    }
}
