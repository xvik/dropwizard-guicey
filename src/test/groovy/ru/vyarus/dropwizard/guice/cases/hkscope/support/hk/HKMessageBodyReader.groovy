package ru.vyarus.dropwizard.guice.cases.hkscope.support.hk

import ru.vyarus.dropwizard.guice.module.installer.feature.jersey.JerseyManaged

import javax.ws.rs.WebApplicationException
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.MultivaluedMap
import javax.ws.rs.ext.MessageBodyReader
import javax.ws.rs.ext.Provider
import java.lang.annotation.Annotation

/**
 * @author Vyacheslav Rusakov
 * @since 19.01.2016
 */
@Provider
@JerseyManaged
class HKMessageBodyReader implements MessageBodyReader<Type> {

    @Override
    boolean isReadable(Class<?> type, java.lang.reflect.Type genericType, Annotation[] annotations, MediaType mediaType) {
        return false
    }

    @Override
    Type readFrom(Class<Type> type, java.lang.reflect.Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException, WebApplicationException {
        return null
    }

    static class Type {}
}
