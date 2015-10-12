package ru.vyarus.dropwizard.guice.cases.innercls

import javax.servlet.ServletException
import javax.ws.rs.core.Response
import javax.ws.rs.ext.ExceptionMapper
import javax.ws.rs.ext.Provider

/**
 * @author Vyacheslav Rusakov 
 * @since 12.10.2015
 */
abstract class AbstractExceptionMapper<T extends Exception> implements ExceptionMapper<T> {

    @Provider
    public static class FooExceptionMapper extends AbstractExceptionMapper<IOException> {
        @Override
        Response toResponse(IOException exception) {
            return null;
        }
    }

    @Provider
    public static class BarExceptionMapper extends AbstractExceptionMapper<ServletException> {
        @Override
        Response toResponse(ServletException exception) {
            return null;
        }
    }
}
