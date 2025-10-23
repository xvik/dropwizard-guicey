package ru.vyarus.dropwizard.guice.test.client.support.sub;

import javax.ws.rs.GET;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.Path;

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
