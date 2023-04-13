package ru.vyarus.dropwizard.guice.support.provider.exceptionmapper

import com.google.inject.Singleton

import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import jakarta.ws.rs.ext.ExceptionMapper
import jakarta.ws.rs.ext.Provider

/**
 * @author Vyacheslav Rusakov 
 * @since 17.04.2015
 */
@Provider
@Singleton
class IOExceptionMapper implements ExceptionMapper<IOException>{

    @Override
    Response toResponse(IOException exception) {
        return Response.status(Response.Status.BAD_REQUEST)
                .type(MediaType.TEXT_PLAIN)
                .entity("ERROR: IO exception!")
                .build()
    }
}
