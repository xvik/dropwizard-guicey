package ru.vyarus.dropwizard.guice.url.resource.support.sub;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

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
