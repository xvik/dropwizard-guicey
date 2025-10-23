package ru.vyarus.dropwizard.guice.url.resource.support.sub;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @author Vyacheslav Rusakov
 * @since 02.10.2025
 */
@Path("/sub2/")
public class SubResource2 {

    @GET
    @Path("/{sm}/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(@PathParam("sm") String sm) {
        return Response.ok().build();
    }
}
