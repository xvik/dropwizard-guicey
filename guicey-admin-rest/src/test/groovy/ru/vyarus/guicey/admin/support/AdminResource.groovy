package ru.vyarus.guicey.admin.support

import jakarta.ws.rs.GET
import jakarta.ws.rs.Path

/**
 * @author Vyacheslav Rusakov 
 * @since 08.08.2015
 */
@Path("/admin")
@ru.vyarus.guicey.admin.rest.AdminResource
class AdminResource {

    @GET
    @Path("/")
    public String hello() {
        return "hello"
    }

}
