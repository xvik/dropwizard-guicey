package ru.vyarus.dropwizard.guice.admin.support

import javax.ws.rs.GET
import javax.ws.rs.Path

/**
 * @author Vyacheslav Rusakov 
 * @since 08.08.2015
 */
@Path("/admin")
@ru.vyarus.dropwizard.guice.admin.rest.AdminResource
class AdminResource {

    @GET
    @Path("/")
    public String hello() {
        return "hello"
    }

}
