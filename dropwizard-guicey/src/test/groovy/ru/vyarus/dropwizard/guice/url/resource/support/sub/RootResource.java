package ru.vyarus.dropwizard.guice.url.resource.support.sub;

import javax.ws.rs.Path;

/**
 * @author Vyacheslav Rusakov
 * @since 02.10.2025
 */
@Path("/root")
public class RootResource {

    @Path("/sub1")
    public SubResource1 sub1() {
        return new SubResource1();
    }
}
