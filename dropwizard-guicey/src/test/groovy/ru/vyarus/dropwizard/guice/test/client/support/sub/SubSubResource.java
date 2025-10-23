package ru.vyarus.dropwizard.guice.test.client.support.sub;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

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
