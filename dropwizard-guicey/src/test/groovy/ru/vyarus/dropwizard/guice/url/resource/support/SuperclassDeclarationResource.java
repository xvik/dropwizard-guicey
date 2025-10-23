package ru.vyarus.dropwizard.guice.url.resource.support;

import javax.ws.rs.core.Response;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;

import java.io.InputStream;

/**
 * @author Vyacheslav Rusakov
 * @since 29.09.2025
 */
public class SuperclassDeclarationResource extends DirectResource {

    @Override
    public Response get(String sm, String q, String hh, String cc, String mm) {
        return super.get(sm, q, hh, cc, mm);
    }

    @Override
    public Response get(MappedBean bean) {
        return super.get(bean);
    }

    @Override
    public void form(String p1, Integer p2) {
        super.form(p1, p2);
    }

    @Override
    public void multipart(String p1, InputStream file1, FormDataBodyPart file2) {
        super.multipart(p1, file1, file2);
    }

    @Override
    public void multipart2(String p1, InputStream file, FormDataContentDisposition fileDisposition) {
        super.multipart2(p1, file, fileDisposition);
    }
}
