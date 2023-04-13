package ru.vyarus.dropwizard.guice.support.provider.exceptionmapper

import com.google.inject.Singleton

import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.Response

/**
 * @author Vyacheslav Rusakov 
 * @since 17.04.2015
 */
@Path("/ex")
@Produces("application/json")
@Singleton
class ExceptionResource {

    @GET
    @Path("/")
    public Response ex() {
        throw new IOException("sample io exception")
    }
}
