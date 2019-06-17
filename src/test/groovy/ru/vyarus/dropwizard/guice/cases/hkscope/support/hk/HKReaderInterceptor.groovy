package ru.vyarus.dropwizard.guice.cases.hkscope.support.hk

import ru.vyarus.dropwizard.guice.module.installer.feature.jersey.JerseyManaged

import javax.ws.rs.WebApplicationException
import javax.ws.rs.ext.Provider
import javax.ws.rs.ext.ReaderInterceptor
import javax.ws.rs.ext.ReaderInterceptorContext

/**
 * @author Vyacheslav Rusakov
 * @since 19.01.2016
 */
@Provider
@JerseyManaged
class HKReaderInterceptor implements ReaderInterceptor {

    @Override
    Object aroundReadFrom(ReaderInterceptorContext context) throws IOException, WebApplicationException {
        return null
    }
}
