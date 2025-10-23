package ru.vyarus.dropwizard.guice.url.resource.support;

import javax.ws.rs.CookieParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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
