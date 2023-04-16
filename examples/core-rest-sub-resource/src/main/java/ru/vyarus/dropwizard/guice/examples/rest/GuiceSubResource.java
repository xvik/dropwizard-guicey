package ru.vyarus.dropwizard.guice.examples.rest;

import ru.vyarus.dropwizard.guice.examples.service.SampleService;

import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.inject.Singleton;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.core.UriInfo;

/**
 * Guice managed sub resource. Uses UriInfo jersey service to implement functionality from
 * hk managed example.
 *
 * @author Vyacheslav Rusakov
 * @since 27.07.2017
 */
@Singleton
public class GuiceSubResource {

    private Provider<UriInfo> uri;
    private SampleService service;

    @Inject
    public GuiceSubResource(Provider<UriInfo> uri, SampleService service) {
        this.uri = uri;
        this.service = service;
    }

    @GET
    public String handle() {
        final String pathParamId = uri.get().getPathParameters().getFirst("pathParamId");
        return "guice " + service.applyState(pathParamId);
    }
}
