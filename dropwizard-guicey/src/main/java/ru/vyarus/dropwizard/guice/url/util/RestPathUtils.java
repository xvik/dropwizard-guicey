package ru.vyarus.dropwizard.guice.url.util;

import org.jspecify.annotations.Nullable;
import ru.vyarus.dropwizard.guice.url.RestPathBuilder;

/**
 * Rest path utils. Useful for building resource paths based on resource classes and methods in application logic
 * (for example, building redirects).
 * <p>
 * There are two benefits of using classes directly:
 * <ul>
 *     <li>Simplified code navigation (obvious where this path leads)</li>
 *     <li>Safety: if the path changed in class, it would be counted here</li>
 * </ul>
 *
 * @author Vyacheslav Rusakov
 * @see ru.vyarus.dropwizard.guice.url.RestPathBuilder
 * @since 25.09.2025
 */
public final class RestPathUtils {

    private RestPathUtils() {
    }

    /**
     * Build resource path from provided resource class.
     * <p>
     * NOTE that this method should not be used for sub-resources, because their {@link jakarta.ws.rs.Path} annotation
     * is ignored (lookup method path used instead).
     *
     * @param resource resource class to build a path for
     * @return path for provided resource (or sub resource) with preserved path parameters placeholders
     * @throws java.lang.IllegalStateException if {@link jakarta.ws.rs.Path} annotation not found in resource class
     *                                         hierarchy
     */
    public static String getResourcePath(final Class<?> resource) {
        return getResourcePath(null, resource);
    }

    /**
     * Build resource path from provided resource class.
     * <p>
     * NOTE that this method should not be used for sub-resources, because their {@link jakarta.ws.rs.Path} annotation
     * is ignored (lookup method path used instead).
     *
     * @param basePath optional base path (could contain String.format() placeholders: %s)
     * @param args variables for path placeholders (String.format() arguments)
     * @param resource resource class to build a path for
     * @return path for provided resource (or sub resource) with preserved path parameters placeholders
     * @throws java.lang.IllegalStateException if {@link jakarta.ws.rs.Path} annotation not found in resource class
     *                                         hierarchy
     */
    public static String getResourcePath(final @Nullable String basePath, final Class<?> resource,
                                         final Object... args) {
        return buildPath(basePath, resource, args).buildTemplate();
    }

    /**
     * Build resource path from provided resource class.
     *
     * @param resource resource class to build a path for
     * @param <T>      resource type
     * @return builder to specify path or query params
     * @throws java.lang.IllegalStateException if {@link jakarta.ws.rs.Path} annotation not found in resource class
     *                                         hierarchy
     */
    public static <T> RestPathBuilder<T> buildPath(final Class<T> resource) {
        return buildPath(null, resource);
    }

    /**
     * Build resource path from provided resource classe.
     *
     * @param basePath optional base path (could contain String.format() placeholders: %s)
     * @param args variables for path placeholders (String.format() arguments)
     * @param resource resource class to build a path for
     * @param <T>      resource type
     * @return builder to specify path or query params
     * @throws java.lang.IllegalStateException if {@link jakarta.ws.rs.Path} annotation not found in resource class
     *                                         hierarchy
     */
    public static <T> RestPathBuilder<T> buildPath(final @Nullable String basePath, final Class<T> resource,
                                                   final Object... args) {
        return new RestPathBuilder<>(basePath != null ? String.format(basePath, args) : null, resource, false);
    }

    /**
     * Build resource path from provided sub-resource class.
     * IMPORTANT: the difference with {@link #buildPath(String, Class, Object...)} is that {@link jakarta.ws.rs.Path}
     * annotation value is ignored for sub-resources (instead, the path from lookup method is used, which must be
     * specified manually).
     *
     * @param basePath sub resource mapping from locator method (could contain String.format() placeholders: %s)
     * @param subResource resource sub-resource class to build a path for
     * @param args variables for path placeholders (String.format() arguments)
     * @return builder to specify path or query params
     * @param <T> sub-resource type
     */
    public static <T> RestPathBuilder<T> buildSubResourcePath(final @Nullable String basePath,
                                                              final Class<T> subResource, final Object... args) {
        return new RestPathBuilder<>(basePath != null ? String.format(basePath, args) : null, subResource, true);
    }
}
