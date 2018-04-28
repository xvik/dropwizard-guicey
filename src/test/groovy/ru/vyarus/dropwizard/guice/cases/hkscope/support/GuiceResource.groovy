package ru.vyarus.dropwizard.guice.cases.hkscope.support

import ru.vyarus.dropwizard.guice.module.installer.feature.jersey.GuiceManaged

import javax.ws.rs.GET
import javax.ws.rs.Path

/**
 * @author Vyacheslav Rusakov
 * @since 19.01.2016
 */
@Path("/guice")
@GuiceManaged
class GuiceResource {

    @GET
    @Path("/foo")
    public String get(){
        return ""
    }
}
