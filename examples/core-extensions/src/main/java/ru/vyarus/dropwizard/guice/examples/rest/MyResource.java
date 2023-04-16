package ru.vyarus.dropwizard.guice.examples.rest;

import ru.vyarus.dropwizard.guice.examples.AppModule;
import ru.vyarus.dropwizard.guice.examples.service.SampleService;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * Resource instantiated by guice. {@link SampleService} interface implementation is configured in guice module
 * {@link AppModule}.
 *
 * {@link ru.vyarus.dropwizard.guice.module.installer.feature.jersey.ResourceInstaller} will force singleton
 * for resource, so manual singleton definition is not required.
 *
 * @author Vyacheslav Rusakov
 * @since 27.01.2016
 */
@Path("/sample")
@Produces(MediaType.APPLICATION_JSON)
public class MyResource {

    @Inject
    private SampleService service;

    @GET
    @Path("/")
    public Response latest() {
        return Response.ok(service.foo()).build();
    }
}
