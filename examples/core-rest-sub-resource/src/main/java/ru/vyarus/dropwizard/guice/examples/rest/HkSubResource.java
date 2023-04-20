package ru.vyarus.dropwizard.guice.examples.rest;

import ru.vyarus.dropwizard.guice.examples.service.SampleService;

import javax.ws.rs.GET;
import javax.ws.rs.PathParam;

/**
 * Sub resource managed by HK. This is useful when advanced context binding is required:
 * here pathParamId parameter is passed from the root resource.
 * <p>
 * IMPORTANT: guice service injection will not work without enabled hk bridge (dependency + option).
 *
 * @author Vyacheslav Rusakov
 * @since 27.07.2017
 */
public class HkSubResource {
    private String id;

    private SampleService service;

    public HkSubResource(@PathParam("pathParamId") String id, SampleService service) {
        this.id = id;
        this.service = service;
    }

    @GET
    public String handle() {
        return "hk " + service.applyState(id);
    }
}
