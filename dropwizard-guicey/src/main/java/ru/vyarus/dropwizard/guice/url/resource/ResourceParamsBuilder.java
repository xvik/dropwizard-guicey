package ru.vyarus.dropwizard.guice.url.resource;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import org.glassfish.jersey.uri.internal.JerseyUriBuilder;
import org.jspecify.annotations.Nullable;
import ru.vyarus.dropwizard.guice.module.installer.util.PathUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Resource path params builder. Allows path and query params declaration.
 * Used as the last step for {@link ru.vyarus.dropwizard.guice.url.RestPathBuilder} (to hide other path-modifying
 * methods).
 */
public abstract class ResourceParamsBuilder {
    protected final JerseyUriBuilder builder = new JerseyUriBuilder();
    protected final Map<String, Object> pathParams = new HashMap<>();

    /**
     * Construct a builder.
     *
     * @param basePath optional base path (to prepend)
     */
    @SuppressWarnings("PMD.ConstructorCallsOverridableMethod")
    public ResourceParamsBuilder(final @Nullable String basePath) {
        if (basePath != null) {
            applyPath(PathUtils.normalizeAbsolutePath(basePath));
        }
    }

    /**
     * Apply path parameter.
     *
     * @param name  parameter name
     * @param value parameter value
     * @return builder instance for chained calls
     */
    public ResourceParamsBuilder pathParam(final String name, final Object value) {
        pathParams.put(name, value);
        return this;
    }

    /**
     * Apply query parameter.
     * <p>
     * Note: jersey api supports multiple query parameters with the same name (in this case multiple parameters
     * added).
     *
     * @param name   parameter name
     * @param values one or more parameter values (array for multiple values)
     * @return builder instance for chained calls
     */
    public ResourceParamsBuilder queryParam(final String name, final Object... values) {
        builder.queryParam(name, values);
        return this;
    }

    /**
     * @return path with preserved path parameters placeholders
     */
    public String buildTemplate() {
        return builder.toTemplate();
    }

    /**
     * @return path with resolved path parameters
     */
    public String build() {
        return pathParams.isEmpty() ? builder.toString()
                : builder.resolveTemplates(pathParams).toString();
    }

    protected void applyPath(final String path, final Object... args) {
        // manually parse query params because otherwise ? would be encoded
        final int idx = path.indexOf('?');
        final String result = String.format(path, args);
        String target = result;
        final Multimap<String, Object> params = LinkedHashMultimap.create();
        if (idx > 0) {
            target = result.substring(0, idx);
            final String query = result.substring(idx + 1);
            Arrays.stream(query.split("&")).forEach(s -> {
                final String[] pair = s.split("=");
                params.put(pair[0], pair.length == 1 ? "" : pair[1]);
            });
        }
        builder.path(target);
        params.keySet().forEach(s -> queryParam(s, params.get(s).toArray(new Object[0])));
    }
}
