package ru.vyarus.dropwizard.guice.test.client.builder.call;

import com.google.common.base.Preconditions;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;
import org.glassfish.jersey.media.multipart.file.StreamDataBodyPart;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * Helper utilities to build multipart objects for rest method analysis api.
 * <p>
 * For the most common case
 * <pre>{@code post(@FormDataParam("file") InputStream stream, @FormDataParam("file")
 * FormDataContentDisposition fileDetail)}
 * </pre>:
 * <pre>{@code .post(multipart.fromClasspath("/some.txt"),
 *                   multipart.disposition("file", "some.txt"))}</pre>.
 * <p>
 * it could be a single field: {@code post(@FormDataParam("file") FormDataBodyPart file)}:
 * <pre>{@code .post(multipart.filePart("/some.txt"))}</pre>
 * or to get file from classpath:
 * <pre>{@code .post(multipart.streamPart("/some.txt"))}</pre>.
 * <p>
 * There might be multiple files for the same field
 * {@code post(@FormDataParam("file") List<FormDataBodyPart> file)}:
 * <pre>{@code .post(Arrays.asList(
 *              multipart.filePart("/some.txt"),
 *              multipart.filePart("/other.txt")))}</pre>.
 * <p>
 * When method only accepts content-disposition mapping, it would be also used with en empty file content
 * (as file content is not required, preserve available data):
 * {@code post(@FormDataParam("file") FormDataContentDisposition file)}
 * <pre>{@code .post(multipart.disposition("file", "some.txt"))}</pre>.
 * <p>
 * The method parameter could be a complete multipart object (with all fields inside):
 * {@code post(FormDataMultiPart multiPart)}: for such cases there is a special builder:
 * <pre>{@code .post(multipart.multipart()
 *                  .field("foo", "val")
 *                  .file("file1", "/some.txt)
 *                  .stream("file2", "/other.txt)
 *                  .build())}</pre>
 *
 * @author Vyacheslav Rusakov
 * @since 10.10.2025
 */
public class MultipartArgumentHelper {

    /**
     * Header would contain both utf-8 and ascii filenames.
     *
     * @param field    field name
     * @param filename file name
     * @return content-disposition header
     */
    public static String createDispositionHeader(final String field, final String filename) {
        return "form-data; name=\"" + field + "\"; filename=\""
                + filename.replaceAll("[^\\p{ASCII}]", "") + "\"; filename*=UTF-8''"
                + URLEncoder.encode(filename, StandardCharsets.UTF_8);
    }

    /**
     * Useful for rest methods declaring input stream and content disposition object.
     *
     * @param path classpath path
     * @return input stream of classpath resource
     * @throws java.lang.IllegalStateException if resource not found
     */
    public InputStream fromClasspath(final String path) {
        final InputStream res = MultipartArgumentHelper.class.getResourceAsStream(path);
        Preconditions.checkState(res != null, "Classpath resource '%s' not found", path);
        return res;
    }

    /**
     * Useful for rest methods declaring input stream and content disposition object.
     *
     * @param path local file path (relative to work dir)
     * @return stream from local file
     * @throws java.lang.IllegalStateException if file does not exist
     * @see #fromClasspath(String) for classpath resource
     * @see #file(String) for obtaining local file
     */
    public InputStream fromFile(final String path) {
        try {
            return Files.newInputStream(file(path).toPath());
        } catch (Exception e) {
            throw new IllegalStateException("Failed to read file '" + path + "' stream", e);
        }
    }

    /**
     * @param path local file path (relative to work dir)
     * @return file object
     * @throws java.lang.IllegalStateException if file does not exist or it is a directory
     */
    public File file(final String path) {
        final File res = new File(path);
        Preconditions.checkState(res.exists() && !res.isDirectory(), "'%s' does not exist or is a directory", path);
        return res;
    }

    /**
     * Create content-disposition object. Useful for resource methods with input stream and disposition object.
     *
     * @param field field name
     * @param file  file to get filename from (may not exist)
     * @return content-disposition object
     */
    public FormDataContentDisposition disposition(final String field, final File file) {
        return disposition(field, file.getName());
    }

    /**
     * Create content-disposition object. Useful for resource methods with input stream and disposition object.
     * File name is applied as utf-8 and ascii.
     *
     * @param field    field name
     * @param filename file name
     * @return content-disposition object
     */
    public FormDataContentDisposition disposition(final String field, final String filename) {
        try {
            return new FormDataContentDisposition(createDispositionHeader(field, filename));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to create content-disposition for field " + field, e);
        }
    }

    /**
     * Create simple data part. {@link org.glassfish.jersey.media.multipart.FormDataBodyPart} could be declared
     * as resource method parameter.
     *
     * @param field field name
     * @param value field value
     * @return simple body part
     */
    public FormDataBodyPart part(final String field, final String value) {
        return new FormDataBodyPart(field, value);
    }

    /**
     * Create file data part. {@link org.glassfish.jersey.media.multipart.FormDataBodyPart} could be declared
     * as resource method parameter (but file part can't!).
     *
     * @param field field name
     * @param file  file object
     * @return file body part
     */
    public FileDataBodyPart filePart(final String field, final File file) {
        return new FileDataBodyPart(field, file);
    }

    /**
     * Create file data part from local file path. {@link org.glassfish.jersey.media.multipart.FormDataBodyPart}
     * could be declared as resource method parameter (but file part can't!).
     *
     * @param field field name
     * @param path  local file path (relative to work dir)
     * @return file body part
     * @throws java.lang.IllegalStateException if target file does not exist, or it is a directory
     */
    public FileDataBodyPart filePart(final String field, final String path) {
        return new FileDataBodyPart(field, file(path));
    }

    /**
     * Create stream data part. {@link org.glassfish.jersey.media.multipart.FormDataBodyPart} could be declared as
     * resource method parameter (but stream part can't!).
     * <p>
     * Important: filename would be missed in this case!
     *
     * @param field  field name
     * @param stream stream
     * @return stream body part
     */
    public StreamDataBodyPart streamPart(final String field, final InputStream stream) {
        return streamPart(field, stream, null);
    }

    /**
     * Create stream data part. {@link org.glassfish.jersey.media.multipart.FormDataBodyPart} could be declared as
     * resource method parameter (but stream part can't!).
     *
     * @param field    field name
     * @param stream   stream
     * @param filename optional filename
     * @return stream body part
     */
    public StreamDataBodyPart streamPart(final String field, final InputStream stream,
                                         @Nullable final String filename) {
        return new StreamDataBodyPart(field, stream, filename);
    }

    /**
     * Create stream data part from classpath resource. {@link org.glassfish.jersey.media.multipart.FormDataBodyPart}
     * could be declared as resource method parameter (but stream part can't!).
     * <p>
     * Note: filename is extracted from provided path.
     *
     * @param field     field name
     * @param classpath classpath path
     * @return stream body part
     */
    public StreamDataBodyPart streamPart(final String field, final String classpath) {
        final int idx1 = classpath.lastIndexOf('/');
        return streamPart(field, fromClasspath(classpath), idx1 >= 0 ? classpath.substring(idx1 + 1) : null);
    }

    /**
     * Multipart object builder. Useful when resource method parameter is
     * {@link org.glassfish.jersey.media.multipart.FormDataMultiPart} (used to obtain all multipart fields).
     *
     * @return multipart object builder.
     */
    public Builder multipart() {
        return new Builder(this);
    }

    /**
     * Multipart object builder.
     */
    public static class Builder {
        private final MultipartArgumentHelper helper;
        private final FormDataMultiPart multiPart = new FormDataMultiPart();

        /**
         * Create multipart builder.
         *
         * @param helper helper object
         */
        public Builder(final MultipartArgumentHelper helper) {
            this.helper = helper;
        }

        /**
         * Add a simple data part.
         *
         * @param field field name
         * @param value field value
         * @return builder instance for chained calls
         */
        public Builder field(final String field, final String value) {
            multiPart.bodyPart(helper.part(field, value));
            return this;
        }

        /**
         * Add a file part with optionally multiple files (when multiple files applied with the same field name).
         *
         * @param field field name
         * @param files one or more files
         * @return builder instance for chained calls
         */
        public Builder file(final String field, final File... files) {
            for (File f : files) {
                multiPart.bodyPart(helper.filePart(field, f));
            }
            return this;
        }

        /**
         * Add a file part with optionally multiple files (when multiple files applied with the same field name)
         * applied from local file paths (relative to work dir).
         *
         * @param field filed name
         * @param paths local file paths (relative to work dir)
         * @return builder instance for chained calls
         * @throws java.lang.IllegalStateException if file does not exist or is directory
         */
        public Builder file(final String field, final String... paths) {
            for (String p : paths) {
                multiPart.bodyPart(helper.filePart(field, p));
            }
            return this;
        }

        /**
         * Add a stream part with optionally multiple streams (when multiple files applied with the same field name)
         * from classpath paths.
         *
         * @param field field name
         * @param paths classpath paths
         * @return builder instance for chained calls
         * @throws java.lang.IllegalStateException if classpath resource not found
         */
        public Builder stream(final String field, final String... paths) {
            for (String p : paths) {
                multiPart.bodyPart(helper.streamPart(field, p));
            }
            return this;
        }

        /**
         * Add stream part.
         * <p>
         * Important: file name would not be provided.
         *
         * @param field  field name
         * @param stream stream instance
         * @return builder instance for chained calls
         */
        public Builder stream(final String field, final InputStream stream) {
            return stream(field, stream, null);
        }

        /**
         * Add stream part with filename.
         *
         * @param field    field name
         * @param stream   stream instance
         * @param filename filename
         * @return builder instance for chained calls
         */
        public Builder stream(final String field, final InputStream stream, @Nullable final String filename) {
            multiPart.bodyPart(helper.streamPart(field, stream, filename));
            return this;
        }

        /**
         * @return multipart object
         */
        public FormDataMultiPart build() {
            Preconditions.checkState(!multiPart.getFields().isEmpty(), "Multipart must have at least one field");
            return multiPart;
        }
    }
}
