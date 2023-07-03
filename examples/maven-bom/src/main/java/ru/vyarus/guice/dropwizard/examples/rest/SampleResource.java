package ru.vyarus.guice.dropwizard.examples.rest;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

/**
 * @author Vyacheslav Rusakov
 * @since 03.07.2023
 */
@Path("/sample")
@Produces("application/json")
public class SampleResource {

    @Inject
    private Provider<HttpServletRequest> requestProvider;

    @GET
    @Path("/")
    public Response ask() {
        final String ip = requestProvider.get().getRemoteAddr();
        return Response.ok(ip).build();
    }
}
