package ru.vyarus.dropwizard.guice.examples.rest;

import com.codahale.metrics.annotation.Timed;
import com.google.inject.Inject;
import io.dropwizard.hibernate.UnitOfWork;
import ru.vyarus.dropwizard.guice.examples.model.Sample;
import ru.vyarus.dropwizard.guice.examples.service.SampleService;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;
import java.util.List;

/**
 * Sample of hibernate usage.
 * Sample method creates entity and return all db entities, so for each request resulted collection would be N+1.
 *
 * @author Vyacheslav Rusakov
 * @since 12.06.2016
 */
@Path("/sample")
@Produces("application/json")
public class SampleResource {

    @Inject
    private SampleService service;

    @GET
    @Path("/")
    @Timed
    @UnitOfWork
    public Response doStaff() {
        final Sample sample = new Sample("sample");
        service.create(sample);
        final List<Sample> res = service.findAll();
        // using response to render entities inside unit of work and avoid lazy load exceptions
        return Response.ok(res).build();
    }
}
