package ru.vyarus.dropwizard.guice.cases.ifaceresource.support

import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.Response

/**
 * @author Vyacheslav Rusakov
 * @since 18.06.2016
 */
@Path("/res")
@Produces('application/json')
interface ResourceContract {

    @GET
    @Path("/")
    String latest();
}
