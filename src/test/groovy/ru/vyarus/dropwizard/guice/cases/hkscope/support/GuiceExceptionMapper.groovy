package ru.vyarus.dropwizard.guice.cases.hkscope.support

import javax.ws.rs.core.Response
import javax.ws.rs.ext.ExceptionMapper
import javax.ws.rs.ext.Provider

/**
 * @author Vyacheslav Rusakov
 * @since 19.01.2016
 */
@Provider
class GuiceExceptionMapper implements ExceptionMapper<IOException> {

    @Override
    Response toResponse(IOException exception) {
        return null
    }
}
