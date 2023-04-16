package ru.vyarus.dropwizard.guice.examples.rest;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;

/**
 * Resource instantiated by guice.
 * <p>
 * {@link ru.vyarus.dropwizard.guice.module.installer.feature.jersey.ResourceInstaller} will force singleton
 * for resource, so manual singleton definition is not required.
 *
 * @author Vyacheslav Rusakov
 * @since 27.01.2016
 */
@Path("/sample")
@Produces("application/json")
public class SampleResource {

    @GET
    @Path("/")
    public Response latest() {
        return Response.ok("foo").build();
    }
}
