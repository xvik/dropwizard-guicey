package ru.vyarus.dropwizard.guice.url.resource.support.sub;

import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;

/**
 * @author Vyacheslav Rusakov
 * @since 02.10.2025
 */
// intentionally no path annotation
public class SubResource1 {

    @Path("/sub2/{name}")
    @Consumes(MediaType.TEXT_PLAIN)
    public SubResource2 sub2(@PathParam("name") String name) {
        return new SubResource2();
    }
}
