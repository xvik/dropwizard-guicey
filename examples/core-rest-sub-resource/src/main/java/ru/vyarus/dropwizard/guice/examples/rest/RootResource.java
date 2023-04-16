package ru.vyarus.dropwizard.guice.examples.rest;

import ru.vyarus.dropwizard.guice.examples.service.SampleService;

import jakarta.inject.Inject;
import jakarta.ws.rs.Path;

/**
 * Root resource is managed by guice. Two sub resources: one with guice and other, managed by hk (and created for
 * each request).
 *
 * @author Vyacheslav Rusakov
 * @since 27.07.2017
 */
@Path("/root")
public class RootResource {

    @Inject
    private SampleService service;
    @Inject
    private GuiceSubResource subResource;

    @Path("{pathParamId}/guice-sub")
    public GuiceSubResource guiceSubResource() {
        service.setState("root1");
        // simply returning guice managed instance (singleton)
        return subResource;
    }

    @Path("{pathParamId}/hk-sub")
    public Class<HkSubResource> hkSubResource() {
        // use state to guarantee the same instance used in sub resource
        service.setState("root2");
        // sub resource will be instantiated by hk2
        return HkSubResource.class;
    }
}
