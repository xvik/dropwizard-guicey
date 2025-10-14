package ru.vyarus.dropwizard.guice.url.resource.support;

import jakarta.ws.rs.CookieParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.MatrixParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * @author Vyacheslav Rusakov
 * @since 14.10.2025
 */
public class NoPathResource {

    @GET
    @Path("/{sm}/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(@PathParam("sm") String sm,
                        @QueryParam("q") String q,
                        @HeaderParam("HH") String hh,
                        @CookieParam("cc") String cc,
                        @MatrixParam("mm") String mm) {
        return Response.ok().build();
    }

}
