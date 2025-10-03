package ru.vyarus.dropwizard.guice.url.resource.support;

import jakarta.ws.rs.core.Response;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;

import java.io.InputStream;

/**
 * @author Vyacheslav Rusakov
 * @since 29.09.2025
 */
public class InterfaceResource implements ResourceDeclaration {

    @Override
    public Response get(String sm,
                        String q,
                        String hh,
                        String cc) {
        return Response.ok().build();
    }

    @Override
    public Response get(MappedBean bean) {
        return Response.ok().build();
    }

    @Override
    public void form(String p1,
                     Integer p2) {
    }

    @Override
    public void multipart(String p1,
                          InputStream file1,
                          FormDataBodyPart file2) {
    }

    @Override
    public void multipart2(String p1,
                           InputStream file,
                           FormDataContentDisposition fileDisposition) {
    }
}
