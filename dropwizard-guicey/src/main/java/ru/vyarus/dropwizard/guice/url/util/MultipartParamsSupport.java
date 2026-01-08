package ru.vyarus.dropwizard.guice.url.util;

import com.google.common.base.Preconditions;
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
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
     * @param value  multipart value
     * @return extracted parameters
     */
    public static Map<String, Object> configureFromMultipart(final Multimap<String, Object> params,
                                                             final Object value) {
        final Map<String, Object> result = new LinkedHashMap<>();
        ((FormDataMultiPart) value).getFields().forEach((s, parts) -> {
            final Object res = computeParameter(s, parts);
            if (res != null) {
                params.put(s, res);
                result.put(s, res);
            }
        });
        return result;
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
    @SuppressWarnings({"PMD.CyclomaticComplexity", "checkstyle:CyclomaticComplexity"})
    public static Object computeParameter(final String name, final Collection<?> values) {
        final List<Object> res = new ArrayList<>();
        final List<InputStream> stream = new ArrayList<>();
        final List<String> fileName = new ArrayList<>();
        for (Object value : values) {
            if (value instanceof FormDataContentDisposition) {
                fileName.add(((FormDataContentDisposition) value).getFileName(true));
            } else if (value instanceof FormDataBodyPart) {
                final Object val = handleFormBodyPart((FormDataBodyPart) value);
                if (val != null) {
                    res.add(val);
                }
            } else if (value instanceof InputStream) {
                // possible case InputStream + FormDataContentDisposition
                stream.add((InputStream) value);
            } else {
                res.add(value);
            }
        }

        if (!fileName.isEmpty() && !stream.isEmpty()) {
            createStreamsWithMetadata(name, fileName, stream, res);
        } else if (!fileName.isEmpty()) {
            createEmptyStreamsForMetadata(name, fileName, res);
        } else if (!stream.isEmpty()) {
            res.addAll(stream);
        }
        return res.isEmpty() ? null : (res.size() == 1 ? res.get(0) : res);
    }

    private static void createStreamsWithMetadata(final String name,
                                                  final List<String> fileName,
                                                  final List<InputStream> stream,
                                                  final List<Object> res) {
        Preconditions.checkState(fileName.size() == stream.size(),
                "Incorrect declaration of form parameter %s: %s file name parts and %s streams declared",
                name, fileName.size(), stream.size());
        for (int i = 0; i < fileName.size(); i++) {
            res.add(new StreamDataBodyPart(name, stream.get(i), fileName.get(i)));
        }
    }

    private static void createEmptyStreamsForMetadata(final String name,
                                                      final List<String> fileName,
                                                      final List<Object> res) {
        // very special case when the resource method declares only content disposition parameters
        // and user specified value(s) for this argument: preserve file names, with fake stream value
        for (String nm : fileName) {
            res.add(new StreamDataBodyPart(name, new ByteArrayInputStream(new byte[0]), nm));
        }
    }


    private static Object handleFormBodyPart(final FormDataBodyPart part) {
        Object res = null;
        if (part instanceof FileDataBodyPart) {
            // raw file
            res = ((FileDataBodyPart) part).getFileEntity();
        } else if (part instanceof StreamDataBodyPart) {
            // stream with filename
            res = part;
        } else if (part.isSimple()) {
            res = part.getValue();
        }
        // otherwise ignore value
        return res;
    }
}
