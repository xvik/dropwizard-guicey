package ru.vyarus.dropwizard.guice.examples.rest;

import ru.vyarus.dropwizard.guice.examples.service.SampleService;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @author Vyacheslav Rusakov
 * @since 30.12.2019
 */
@Path("/sample3")
@Produces(MediaType.APPLICATION_JSON)
public class MyResourceFromBinding {

    @Inject
    private SampleService service;

    @GET
    @Path("/")
    public Response latest() {
        return Response.ok(service.foo()).build();
    }
}
