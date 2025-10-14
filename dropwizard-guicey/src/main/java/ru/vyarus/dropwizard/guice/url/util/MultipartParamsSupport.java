package ru.vyarus.dropwizard.guice.url.util;

import com.google.common.collect.Multimap;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;
import org.glassfish.jersey.media.multipart.file.StreamDataBodyPart;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Map;

/**
 * Multipart parameters handling. Externalized into a separate class to support general paramteres usage without
 * multipart dependency.
 *
 * @author Vyacheslav Rusakov
 * @since 03.10.2025
 */
public final class MultipartParamsSupport {

    private MultipartParamsSupport() {
    }

    /**
     * @param annotation param annotation
     * @return param annotation value
     */
    public static String getParamName(final Annotation annotation) {
        return ((FormDataParam) annotation).value();
    }

    /**
     * Parse entire multipart object {@link FormDataMultiPart} (it might be used as rest method parameter to
     * aggregate all fields).
     *
     * @param params params collector
     * @param value multipart value
     */
    public static void configureFromMultipart(final Multimap<String, Object> params, final Object value) {
        ((FormDataMultiPart) value).getFields().forEach((s, parts) -> {
            final Object res = computeParameter(s, parts);
            if (res != null) {
                params.put(s, res);
            }
        });
    }

    /**
     * Multipart parameters could be handled with multuiple aprameters, for example:
     * {@code method(@FormDaraParam("file") InputStream in, @FormDataParam("file") FormDataContentDisposition info)}.
     *
     * @param params    target map to store parameters
     * @param multipart recorded field arguments
     */
    public static void processFormParams(final Map<String, Object> params,
                                         final Multimap<String, Object> multipart) {
        for (String key : multipart.keySet()) {
            final Collection<Object> values = multipart.get(key);
            final Object res = computeParameter(key, values);
            if (res != null) {
                params.put(key, res);
            }
        }
    }

    /**
     * Recognize multipart value from the method call arguments.
     *
     * @param name   field name
     * @param values all arguments for the field
     * @return recognized multipart value or null
     */
    @SuppressWarnings({"PMD.CyclomaticComplexity", "checkstyle:ReturnCount", "checkstyle:CyclomaticComplexity"})
    public static Object computeParameter(final String name, final Collection<?> values) {
        InputStream stream = null;
        String fileName = null;
        final boolean singleValue = values.size() == 1;
        for (Object value : values) {
            if (value instanceof FormDataContentDisposition) {
                fileName = ((FormDataContentDisposition) value).getFileName(true);
            } else if (value instanceof FormDataBodyPart) {
                final FormDataBodyPart part = (FormDataBodyPart) value;
                if (part instanceof FileDataBodyPart) {
                    // raw file
                    return ((FileDataBodyPart) part).getFileEntity();
                } else if (value instanceof StreamDataBodyPart) {
                    // stream with filename
                    return value;
                } else if (part.isSimple()) {
                    return part.getValue();
                }
                // otherwise ignore value
            } else if (value instanceof InputStream) {
                if (singleValue) {
                    return value;
                } else {
                    // case InputStream + FormDataContentDisposition
                    stream = (InputStream) value;
                }
            } else {
                return value;
            }
        }
        if (fileName != null && stream != null) {
            return new StreamDataBodyPart(name, stream, fileName);
        } else if (fileName != null) {
            // very special case when the resource method declares only content disposition parameters
            // and user specified value(s) for this argument: preserve file names, with fake stream value
            return new StreamDataBodyPart(name, new ByteArrayInputStream(new byte[0]), fileName);
        }
        return null;
    }
}
