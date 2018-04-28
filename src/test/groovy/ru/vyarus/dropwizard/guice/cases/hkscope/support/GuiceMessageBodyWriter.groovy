package ru.vyarus.dropwizard.guice.cases.hkscope.support

import ru.vyarus.dropwizard.guice.module.installer.feature.jersey.GuiceManaged

import javax.ws.rs.WebApplicationException
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.MultivaluedMap
import javax.ws.rs.ext.MessageBodyWriter
import javax.ws.rs.ext.Provider
import java.lang.annotation.Annotation

/**
 * @author Vyacheslav Rusakov
 * @since 19.01.2016
 */
@Provider
@GuiceManaged
class GuiceMessageBodyWriter implements MessageBodyWriter<Type> {

    @Override
    boolean isWriteable(Class<?> type, java.lang.reflect.Type genericType, Annotation[] annotations, MediaType mediaType) {
        return false
    }

    @Override
    long getSize(Type type, Class<?> type2, java.lang.reflect.Type genericType, Annotation[] annotations, MediaType mediaType) {
        return 0
    }

    @Override
    void writeTo(Type type, Class<?> type2, java.lang.reflect.Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException {

    }

    static class Type {}
}
