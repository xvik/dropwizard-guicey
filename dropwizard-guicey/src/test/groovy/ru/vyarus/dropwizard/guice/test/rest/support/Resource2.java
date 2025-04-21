package ru.vyarus.dropwizard.guice.test.rest.support;

import com.google.common.base.Preconditions;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

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
