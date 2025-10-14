package ru.vyarus.dropwizard.guice.test.client.util;

import jakarta.ws.rs.core.Response;
import org.eclipse.jetty.http.HttpHeader;
import org.jspecify.annotations.Nullable;
import ru.vyarus.dropwizard.guice.test.client.builder.util.conf.MultipartSupport;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * File download from response utility.
 *
 * @author Vyacheslav Rusakov
 * @since 09.10.2025
 */
public final class FileDownloadUtil {

    private FileDownloadUtil() {
    }

    /**
     * Download a file from the response.
     * <p>
     * Limitation: does not support multipart responses. Use {@link #parseFileName(String)}
     * and {@link #saveFile(String, InputStream, Path)} manually directly for multipart chunks.
     *
     * @param response response object
     * @param dir target directory to save the file in
     * @return downloaded ile
     */
    public static Path download(final Response response, final Path dir) {
        final String disposition = response.getHeaderString(HttpHeader.CONTENT_DISPOSITION.toString());
        String filename = "download_" + System.currentTimeMillis();
        if (disposition != null) {
            final String name = readFilename(response);
            if (name != null) {
                filename = name;

            }
        }
        return saveFile(filename, response.readEntity(InputStream.class), dir);
    }

    /**
     * Save file from input stream to target directory. If a file with the same name already exists, an index will be
     * added to the name.
     *
     * @param name file name (could be without extension)
     * @param input file input stream
     * @param dir target directory
     * @return downloaded file
     */
    public static Path saveFile(final String name, final InputStream input, final Path dir) {
        final int idx = name.lastIndexOf('.');
        final String base = idx > 0 ? name.substring(0, idx) : name;
        final String ext = idx > 0 ? name.substring(idx) : "";
        Path target = dir.resolve(name);
        int cnt = 0;
        while (target.toFile().exists()) {
            target = dir.resolve(base + "(" + (++cnt) + ")" + ext);
        }

        try {
            Files.copy(input, target);
            return target;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to download file", e);
        }
    }

    /**
     * Use jersey header parser when the multipart jar is available. Otherwise, fallback to less accurate manual
     * parsing.
     *
     * @param response response object to read header from
     * @return file name from content-disposition header or null if header not found
     */
    @Nullable
    public static String readFilename(final Response response) {
        if (MultipartCheck.isEnabled()) {
            // when multipart is available, use provided implementation ro parse header (more accurate)
            return MultipartSupport.readFilename(response);
        }
        // when multipart id not available, parse header manually
        return parseFileName(response);
    }

    /**
     * Less accurate version of {@link #readFilename(Response)} with simplified header parsing. Used when the multipart
     * jar is not available (and so jersey native parsing can't be used).
     *
     * @param response response object to read header from
     * @return file name from content-disposition header or null if header not found
     */
    @Nullable
    public static String parseFileName(final Response response) {
        final String header = response.getHeaderString(HttpHeader.CONTENT_DISPOSITION.toString());
        if (header != null) {
            return parseFileName(header);
        }
        return null;
    }

    /**
     * Less accurate version of {@link #readFilename(Response)} with simplified header parsing. Used when the multipart
     * jar is not available (and so jersey native parsing can't be used).
     *
     * @param header content-disposition header
     * @return parsed file name
     */
    public static String parseFileName(final String header) {
        String filename = null;
        for (String part : header.split(";")) {
            String chunk = part.trim();
            if (chunk.startsWith("filename=")) {
                filename = unquote(chunk.substring("filename=".length()).trim());
            }
            if (chunk.startsWith("filename*=")) {
                chunk = unquote(chunk.substring("filename*=".length()).trim());
                if (chunk.contains("''")) {
                    final String[] parts = chunk.split("''");
                    final String encoding = parts[0];
                    try {
                        filename = URLDecoder.decode(parts[1], encoding);
                    } catch (UnsupportedEncodingException e) {
                        throw new IllegalStateException("Failed to decode file name: " + parts[1], e);
                    }
                }
                break;
            }
        }
        return filename;
    }

    private static String unquote(final String value) {
        String res = value;
        if (res.contains("\"")) {
            res = res.substring(1);
        }
        if (res.endsWith("\"")) {
            res = res.substring(0, res.length() - 1);
        }
        return res;
    }
}
