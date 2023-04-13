package ru.vyarus.guicey.admin.support

import jakarta.ws.rs.GET
import jakarta.ws.rs.Path

/**
 * @author Vyacheslav Rusakov 
 * @since 08.08.2015
 */
@Path("/hybrid")
class HybridResource {

    @GET
    @Path("/hello")
    public String hello() {
        return "hello"
    }

    @GET
    @Path("/admin")
    @ru.vyarus.guicey.admin.rest.AdminResource
    public String admin() {
        return "admin"
    }
}
