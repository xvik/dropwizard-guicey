package ru.vyarus.dropwizard.guice.cases.hkscope.support.hk

import ru.vyarus.dropwizard.guice.module.installer.feature.jersey.HK2Managed

import javax.ws.rs.GET
import javax.ws.rs.Path

/**
 * @author Vyacheslav Rusakov
 * @since 19.01.2016
 */
@Path("/hk")
@HK2Managed
class HKResource {
    @GET
    @Path("/foo")
    public String get() {
        return ""
    }
}
