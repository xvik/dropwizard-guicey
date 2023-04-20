package ru.vyarus.dropwizard.guice.examples.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

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
