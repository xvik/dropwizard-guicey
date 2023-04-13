package ru.vyarus.dropwizard.guice.support.feature

import ru.vyarus.dropwizard.guice.module.installer.scanner.InvisibleForScanner

import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.Response

/**
 * Checks that invisible annotation handled by installers.
 * @author Vyacheslav Rusakov 
 * @since 04.09.2014
 */
@InvisibleForScanner
@Path("/invisible")
@Produces('application/json')
class InvisibleResource {

    @GET
    @Path("/")
    public Response latest() {
        return Response.ok().build();
    }
}