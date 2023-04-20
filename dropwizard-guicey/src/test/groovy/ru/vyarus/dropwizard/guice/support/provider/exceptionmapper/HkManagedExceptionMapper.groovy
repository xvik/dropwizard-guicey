package ru.vyarus.dropwizard.guice.support.provider.exceptionmapper

import com.google.inject.Singleton
import ru.vyarus.dropwizard.guice.module.installer.feature.jersey.JerseyManaged

import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response
import javax.ws.rs.ext.ExceptionMapper
import javax.ws.rs.ext.Provider

/**
 * @author Vyacheslav Rusakov 
 * @since 17.04.2015
 */
@Provider
@Singleton
@JerseyManaged
class HkManagedExceptionMapper implements ExceptionMapper<IOException> {

    @Override
    Response toResponse(IOException exception) {
        return Response.status(Response.Status.BAD_REQUEST)
                .type(MediaType.TEXT_PLAIN)
                .entity("ERROR: IO exception!")
                .build()
    }
}
