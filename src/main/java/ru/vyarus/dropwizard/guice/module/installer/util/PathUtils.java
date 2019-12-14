package ru.vyarus.dropwizard.guice.module.installer.util;

import com.google.common.base.CharMatcher;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;

import java.util.regex.Pattern;

/**
 * Path and URL utils. Cover common cases when path must be cleaned (remove duplicate slashes and round backslashes),
 * trim leading/trailing slashes etc.
 *
 * @author Vyacheslav Rusakov
 * @since 06.12.2018
 */
public final class PathUtils {

    public static final String SLASH = "/";
    private static final Pattern PATH_DIRTY_SLASHES = Pattern.compile("\\s*/\\s*(/+\\s*)?");
    private static final CharMatcher TRIM_SLASH = CharMatcher.is('/');
    private static final CharMatcher TRIM_STAR = CharMatcher.is('*');

    private PathUtils() {
    }

    /**
     * Combine parts into correct path avoiding duplicate slashes and replacing backward slashes. Null and empty
     * parts are ignored. No leading or trailing slash appended.
     *
     * @param parts path parts
     * @return combined path from supplied parts
     */
    public static String path(final String... parts) {
        for (int i = 0; i < parts.length; i++) {
            // important because if first (or last) provided chank is "" then resulted path will unexpectedly
            // start / end with slash
            parts[i] = Strings.emptyToNull(parts[i]);
        }
        return normalize(Joiner.on(SLASH).skipNulls().join(parts));
    }

    /**
     * Cleanup duplicate slashes and replace backward slashes.
     *
     * @param path path to cleanup
     * @return path with canonical slashes
     */
    public static String normalize(final String path) {
        final String fixedBackslashes = path.replace('\\', '/');
        return PATH_DIRTY_SLASHES.matcher(fixedBackslashes).replaceAll(SLASH).trim();
    }

    /**
     * @param path path
     * @return path started with slash (original path if it already starts with slash)
     */
    public static String leadingSlash(final String path) {
        return path.startsWith(SLASH) ? path : SLASH + path;
    }

    /**
     * Exception: slash is not applied to empty string because in this case it would become leading slash too
     * (may not be desired behaviour).
     *
     * @param path path
     * @return path ended with slash (original path if it already ends with slash)
     */
    public static String trailingSlash(final String path) {
        if (path.isEmpty()) {
            return path;
        }
        return path.endsWith(SLASH) ? path : path + SLASH;
    }

    /**
     * Method used to cleanup wildcard paths like "/*" into "/".
     *
     * @param path path
     * @return path without leading / trailing stars
     */
    public static String trimStars(final String path) {
        return TRIM_STAR.trimFrom(path);
    }

    /**
     * Note: assumed that leading and trailing slashed are not backslashes (in this case use
     * {@link #normalize(String)} first).
     *
     * @param path path
     * @return path without leading / trailing slashes
     */
    public static String trimSlashes(final String path) {
        return TRIM_SLASH.trimFrom(path);
    }

    /**
     * Note: assumed that leading slash is not backslash (in this case use
     * {@link #normalize(String)} first).
     *
     * @param path path
     * @return path without leading slash
     */
    public static String trimLeadingSlash(final String path) {
        return TRIM_SLASH.trimLeadingFrom(path);
    }

    /**
     * Note: assumed that trailing slash is not backslash (in this case use
     * {@link #normalize(String)} first).
     *
     * @param path path
     * @return path without trailing slash
     */
    public static String trimTrailingSlash(final String path) {
        return TRIM_SLASH.trimTrailingFrom(path);
    }

    /**
     * Method used to normalize and remove leading slash to convert path into relative if it starts from slash.
     * This is important for html pages with base tag declared: relative paths correctly resolved relative to
     * application root.
     *
     * @param path path to make relative
     * @return relative path (without leading slash)
     */
    public static String relativize(final String path) {
        return trimLeadingSlash(normalize(path));
    }

    /**
     * Returned location path does not contain leading slash because its not needed for direct classpath resource
     * loading.
     *
     * @param cls class
     * @return class location path
     */
    public static String packagePath(final Class cls) {
        return trailingSlash(cls.getPackage().getName().replace(".", PathUtils.SLASH));
    }

    /**
     * Normalization for sub section (sub folder) url path. Rules:
     * <ul>
     *     <li>Backslashes replaced with '/'</li>
     *     <li>Url must not starts with '/'</li>
     *     <li>Url must end with '/' (to prevent false sub-string matches when path used for matches)</li>
     * </ul>
     * <p>
     * The difference with {@link #relativize(String)} is that trailing slash is applied (which is wrong for urls!).
     * <p>
     * IMPORTANT: '/' will become '' (here leading and trailing slash rules conflict, but leading rule wins as
     * more important in this case).
     *
     * @param path path
     * @return normalized url
     */
    public static String normalizeRelativePath(final String path) {
        final String cleanPath = normalize(path);
        // do not apply end slash to empty path to not confuse with leading slash
        // NOTE '/' will become '' - intentional!
        return cleanPath.isEmpty() ? cleanPath : trailingSlash(trimSlashes(cleanPath));
    }

    /**
     * Normalization for classpath resource path. Rules:
     * <ul>
     *     <li>Path use '/' as separator</li>
     *     <li>Backslashes replaced with '/'</li>
     *     <li>Path must not start with '/'</li>
     *     <li>Path ends with '/'</li>
     * </ul>
     *
     * @param path classpath path (with '.' or '/')
     * @return normalized classpath path
     */
    public static String normalizeClasspathPath(final String path) {
        return normalizeRelativePath(path.replace('.', '/'));
    }
}
