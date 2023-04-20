package ru.vyarus.dropwizard.guice.cases.hkscope.support.hk

import ru.vyarus.dropwizard.guice.module.installer.feature.jersey.JerseyManaged

import javax.ws.rs.WebApplicationException
import javax.ws.rs.ext.Provider
import javax.ws.rs.ext.WriterInterceptor
import javax.ws.rs.ext.WriterInterceptorContext

/**
 * @author Vyacheslav Rusakov
 * @since 19.01.2016
 */
@Provider
@JerseyManaged
class HKWriterInterceptor implements WriterInterceptor {

    @Override
    void aroundWriteTo(WriterInterceptorContext context) throws IOException, WebApplicationException {

    }
}
