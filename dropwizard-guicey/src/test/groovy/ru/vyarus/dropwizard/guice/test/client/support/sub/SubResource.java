package ru.vyarus.dropwizard.guice.test.client.support.sub;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

/**
 * @author Vyacheslav Rusakov
 * @since 12.10.2025
 */
@Path("/sub")
public class SubResource {

    @Path("/get")
    @GET
    public String get() {
        return "ok";
    }

    @Path("/sub2")
    public SubSubResource sub() {
        return new SubSubResource();
    }
}
