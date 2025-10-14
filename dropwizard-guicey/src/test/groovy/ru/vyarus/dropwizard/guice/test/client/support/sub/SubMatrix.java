package ru.vyarus.dropwizard.guice.test.client.support.sub;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.MatrixParam;
import jakarta.ws.rs.Path;

/**
 * @author Vyacheslav Rusakov
 * @since 13.10.2025
 */
@Path("/msub")
public class SubMatrix {

    @Path("/get")
    @GET
    public String get(@MatrixParam("s1") String s1) {
        return s1;
    }
}
