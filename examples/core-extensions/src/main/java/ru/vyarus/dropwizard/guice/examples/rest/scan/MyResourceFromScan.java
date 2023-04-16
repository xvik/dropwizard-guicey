package ru.vyarus.dropwizard.guice.examples.rest.scan;

import ru.vyarus.dropwizard.guice.examples.service.SampleService;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * @author Vyacheslav Rusakov
 * @since 30.12.2019
 */
@Path("/sample2")
@Produces(MediaType.APPLICATION_JSON)
public class MyResourceFromScan {

    @Inject
    private SampleService service;

    @GET
    @Path("/")
    public Response latest() {
        return Response.ok(service.foo()).build();
    }
}
