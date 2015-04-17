package ru.vyarus.dropwizard.guice.support.provider.exceptionmapper

import com.google.inject.Singleton

import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.xml.ws.Response

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
