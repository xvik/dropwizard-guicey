package ru.vyarus.dropwizard.guice.cases.ifaceresource.support

import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.Response

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
