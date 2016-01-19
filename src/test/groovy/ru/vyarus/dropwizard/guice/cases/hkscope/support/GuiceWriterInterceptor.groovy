package ru.vyarus.dropwizard.guice.cases.hkscope.support

import javax.ws.rs.WebApplicationException
import javax.ws.rs.ext.Provider
import javax.ws.rs.ext.WriterInterceptor
import javax.ws.rs.ext.WriterInterceptorContext

/**
 * @author Vyacheslav Rusakov
 * @since 19.01.2016
 */
@Provider
class GuiceWriterInterceptor implements WriterInterceptor {

    @Override
    void aroundWriteTo(WriterInterceptorContext context) throws IOException, WebApplicationException {

    }
}
