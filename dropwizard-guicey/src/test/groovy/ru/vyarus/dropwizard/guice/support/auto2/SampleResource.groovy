package ru.vyarus.dropwizard.guice.support.auto2

import javax.ws.rs.GET
import javax.ws.rs.Path

/**
 * @author Vyacheslav Rusakov
 * @since 29.12.2022
 */
@Path("/sample")
class SampleResource {

    @GET
    String get() {
        return ""
    }
}
