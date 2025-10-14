package ru.vyarus.dropwizard.guice.test.client.support;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.MatrixParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.PathSegment;
import ru.vyarus.dropwizard.guice.test.client.support.sub.SubMatrix;

/**
 * @author Vyacheslav Rusakov
 * @since 13.10.2025
 */
@Path("/matrix")
public class MatrixResource {

    @Path("/get")
    @GET
    public String get(@MatrixParam("p1") String p1, @MatrixParam("p2") String p2) {
        return p1 + ";" + p2;
    }

    // /get2;a=1/
    @Path("/{vars:get2}/op/")
    @GET
    public String get(@PathParam("vars") PathSegment vars, @MatrixParam("p1") String p1, @MatrixParam("p2") String p2) {
        return p1 + ";" + p2;
    }

    // /sub;a=1/
    @Path("/{vars:sub}")
    public SubMatrix sub(@PathParam("vars") PathSegment vars) {
        return new SubMatrix();
    }
}
