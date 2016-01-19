package ru.vyarus.dropwizard.guice.cases.hkscope.support

import javax.ws.rs.GET
import javax.ws.rs.Path

/**
 * @author Vyacheslav Rusakov
 * @since 19.01.2016
 */
@Path("/guice")
class GuiceResource {

    @GET
    @Path("/foo")
    public String get(){
        return ""
    }
}
