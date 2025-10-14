package ru.vyarus.dropwizard.guice.test.client.support.sub;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

/**
 * @author Vyacheslav Rusakov
 * @since 12.10.2025
 */
@Path("/sub2")
public class SubSubResource {

    @Path("/get")
    @GET
    public String get() {
        return "ko";
    }
}
