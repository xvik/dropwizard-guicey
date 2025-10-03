package ru.vyarus.dropwizard.guice.url.util;

import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;

import java.lang.annotation.Annotation;
import java.util.Map;

/**
 * Multipart parameters handling. Externalized into a separate class to support general paramteres usage without
 * multipart dependency.
 *
 * @author Vyacheslav Rusakov
 * @since 03.10.2025
 */
public class MultipartParamsSupport {

    /**
     * Recognize multipart value from the method call.
     *
     * @param params form parameters map
     * @param annotation {@link org.glassfish.jersey.media.multipart.FormDataParam} annotation
     * @param value annotated method parameter value
     */
    public static void addFormParam(final Map<String, Object> params,
                                    final Annotation annotation,
                                    final Object value) {
        if (value instanceof FormDataContentDisposition) {
            return;
        }
        if (value instanceof FormDataBodyPart) {
            final FormDataBodyPart part = (FormDataBodyPart) value;
            if (part instanceof FileDataBodyPart) {
                final FileDataBodyPart filePart = (FileDataBodyPart) part;
                params.put(filePart.getName(), filePart.getFileEntity());
            } else if (part.isSimple()) {
                params.put(part.getName(), part.getValue());
            }
            // otherwise ignore value
        } else {
            final String name = ((FormDataParam) annotation).value();
            params.put(name, value);
        }
    }
}
