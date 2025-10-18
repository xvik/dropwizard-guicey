package ru.vyarus.dropwizard.guice.test.client.builder.util.conf;

import jakarta.ws.rs.client.WebTarget;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Helper utilities to apply jersey request configuration.
 *
 * @author Vyacheslav Rusakov
 * @since 22.09.2025
 */
public final class JerseyRequestConfigurer {

    private JerseyRequestConfigurer() {
    }

    /**
     * Supplier could return either single value or {@link java.lang.Iterable} or array to put multiple query
     * parameters with tha same name.
     *
     * @param target      original target
     * @param queryParams query params map
     * @return modified or the same target
     */
    public static WebTarget applyQueryParams(final WebTarget target,
                                             final Map<String, ? extends Supplier<Object>> queryParams) {
        return applyParams(target, queryParams, WebTarget::queryParam);
    }

    /**
     * Supplier could return either single value or {@link java.lang.Iterable} or array to put multiple matrix
     * parameters with tha same name.
     *
     * @param target       original target
     * @param matrixParams query params map
     * @return modified or the same target
     */
    public static WebTarget applyMatrixParams(final WebTarget target,
                                              final Map<String, ? extends Supplier<Object>> matrixParams) {
        return applyParams(target, matrixParams, WebTarget::matrixParam);
    }

    /**
     * Supplier could return either class or instance. Supplier could be null to register by type.
     *
     * @param target     original target
     * @param extensions jersey extensions to apply to client request
     */
    public static void applyExtensions(final WebTarget target,
                                       final Map<Class<?>, ? extends Supplier<?>> extensions) {
        for (Map.Entry<Class<?>, ? extends Supplier<?>> entry : extensions.entrySet()) {
            final Object ext = entry.getValue().get();
            if (ext instanceof Class<?>) {
                target.register((Class<?>) ext);
            } else {
                target.register(ext);
            }
        }
    }

    private static WebTarget applyParams(final WebTarget target,
                                         final Map<String, ? extends Supplier<Object>> params,
                                         final ParamHandler applier) {
        WebTarget out = target;
        for (Map.Entry<String, ? extends Supplier<Object>> entry : params.entrySet()) {
            final Object value = entry.getValue().get();
            // special support for multiple values (in this case multiple query/matrix params must be applied)
            final List<Object> multiple = new ArrayList<>();
            if (value instanceof Iterable<?>) {
                ((Iterable<?>) value).forEach(multiple::add);
            } else if (value.getClass().isArray()) {
                multiple.addAll(Arrays.asList((Object[]) value));
            } else {
                multiple.add(value);
            }
            final Object[] val = multiple.toArray(new Object[0]);
            out = applier.apply(out, entry.getKey(), val);
        }
        return out;
    }

    /**
     * Parameter applier.
     */
    @FunctionalInterface
    public interface ParamHandler {

        /**
         * Apply parameter.
         *
         * @param target web target
         * @param name   parameter name
         * @param values values
         * @return modified web target
         */
        WebTarget apply(WebTarget target, String name, Object... values);
    }
}
