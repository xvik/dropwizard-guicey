package ru.vyarus.dropwizard.guice.test.rest.support;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * @author Vyacheslav Rusakov
 * @since 22.02.2025
 */
@Provider
public class RestExceptionMapper implements ExceptionMapper<Exception> {

    public boolean called = false;

    @Override
    public Response toResponse(Exception exception) {
        called = true;
        return Response.serverError().entity(exception.getMessage()).build();
    }
}
