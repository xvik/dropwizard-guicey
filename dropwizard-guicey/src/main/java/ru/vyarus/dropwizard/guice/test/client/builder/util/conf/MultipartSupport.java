package ru.vyarus.dropwizard.guice.test.client.builder.util.conf;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import org.glassfish.jersey.media.multipart.BodyPart;
import org.glassfish.jersey.media.multipart.ContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;
import org.glassfish.jersey.media.multipart.file.StreamDataBodyPart;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Map;

/**
 * Build multipart form. Requires 'org.glassfish.jersey.media:jersey-media-multipart' dependency.
 *
 * @author Vyacheslav Rusakov
 * @since 15.09.2025
 */
public final class MultipartSupport {

    private MultipartSupport() {
    }

    /**
     * @param value value to check
     * @return true if value is a multipart value
     */
    public static boolean isMultipartValue(final Object value) {
        return value instanceof BodyPart || value instanceof File || value instanceof InputStream;
    }

    /**
     * Build a multipart form entity from parameters. Any {@link org.glassfish.jersey.media.multipart.BodyPart}
     * value used as-is (e.g. manually constructed {@link org.glassfish.jersey.media.multipart.FormDataBodyPart}).
     * File could be specified as {@link java.io.File} or {@link java.io.InputStream}. All other values are converted
     * to string.
     * <p>
     * String conversion specifics:
     * <ul>
     *     <li>Date fields string conversion could be customized with date formatters (one for java.util and other for
     *        java.time api).</li>
     *     <li>Null values converted to ""</li>
     *     <li>First level collection assumed to be a multi-value. Underlying collections are converted to strings.</li>
     *     <li>By default, call toString on a provided object</li>
     * </ul>
     *
     * @param formParams     form parameters
     * @param dateFormat     java.util dates formatter
     * @param dateTimeFormat java.time dates formatter
     * @return form multipart entity
     */
    public static Entity<?> buildMultipart(final Map<String, Object> formParams,
                                           final @Nullable DateFormat dateFormat,
                                           final @Nullable DateTimeFormatter dateTimeFormat) {
        final FormDataMultiPart mp = new FormDataMultiPart();
        formParams.forEach((key, value) -> applyParam(mp, key, value, dateFormat, dateTimeFormat));
        return Entity.entity(mp, mp.getMediaType());
    }

    /**
     * @param response response object
     * @return file name from content-disposition header or null if header not found
     */
    @Nullable
    public static String readFilename(final Response response) {
        final String header = response.getHeaderString("Content-Disposition");
        if (header != null) {
            return readFilename(header);
        }
        return null;
    }

    /**
     * @param header content-disposition header
     * @return filename from content-disposition header
     */
    public static String readFilename(final String header) {
        try {
            final ContentDisposition contentDisposition = new ContentDisposition(header);
            return contentDisposition.getFileName(true);
        } catch (ParseException e) {
            throw new IllegalStateException("Failed to parse Content-Disposition header", e);
        }
    }

    private static void applyParam(final FormDataMultiPart mp, final String key, final Object value,
                                   final @Nullable DateFormat dateFormat,
                                   final @Nullable DateTimeFormatter dateTimeFormat) {
        BodyPart bodyPart = null;
        if (value instanceof BodyPart) {
            bodyPart = (BodyPart) value;
        } else if (value instanceof File) {
            bodyPart = new FileDataBodyPart(key, (File) value);
        } else if (value instanceof InputStream) {
            bodyPart = new StreamDataBodyPart(key, (InputStream) value);
        }
        if (bodyPart != null) {
            mp.bodyPart(bodyPart);
        } else {
            if (value instanceof Collection) {
                for (Object val : (Collection<?>) value) {
                    applyParam(mp, key, val, dateFormat, dateTimeFormat);
                }
            } else if (value.getClass().isArray()) {
                for (Object val : (Object[]) value) {
                    applyParam(mp, key, val, dateFormat, dateTimeFormat);
                }
            } else {
                mp.field(key, FormParamsSupport.parameterToString(value, dateFormat, dateTimeFormat));
            }
        }
    }
}
