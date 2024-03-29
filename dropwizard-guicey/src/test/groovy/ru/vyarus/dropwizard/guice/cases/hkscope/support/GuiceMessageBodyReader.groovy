package ru.vyarus.dropwizard.guice.cases.hkscope.support

import ru.vyarus.dropwizard.guice.module.installer.feature.jersey.GuiceManaged

import jakarta.ws.rs.WebApplicationException
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.MultivaluedMap
import jakarta.ws.rs.ext.MessageBodyReader
import jakarta.ws.rs.ext.Provider
import java.lang.annotation.Annotation

/**
 * @author Vyacheslav Rusakov
 * @since 19.01.2016
 */
@Provider
@GuiceManaged
class GuiceMessageBodyReader implements MessageBodyReader<Type> {

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
