package ru.vyarus.dropwizard.guice.test.client.builder.util;

import javax.ws.rs.Consumes;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * In theory, {@code response.readEntity(Void.class)} should completely ignore response content (if present), but,
 * actually jersey throws no mapper found error. This mapper should be used as a workaround: to completely ignore
 * the response body.
 *
 * @author Vyacheslav Rusakov
 * @since 17.09.2025
 * @see ru.vyarus.dropwizard.guice.test.client.builder.TestClientRequestBuilder#noBodyMappingForVoid()
 */
@Provider
@Consumes
public class VoidBodyReader implements MessageBodyReader<Void> {

    @Override
    public boolean isReadable(final Class<?> type,
                              final Type genericType,
                              final Annotation[] annotations,
                              final MediaType mediaType) {
        return type.equals(Void.class);
    }

    @Override
    public Void readFrom(final Class<Void> type,
                         final Type genericType,
                         final Annotation[] annotations,
                         final MediaType mediaType,
                         final MultivaluedMap<String, String> httpHeaders,
                         final InputStream entityStream) throws IOException, WebApplicationException {
        return null;
    }
}
