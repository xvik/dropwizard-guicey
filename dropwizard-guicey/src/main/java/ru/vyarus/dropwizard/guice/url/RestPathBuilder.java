package ru.vyarus.dropwizard.guice.url;

import org.jspecify.annotations.Nullable;
import ru.vyarus.dropwizard.guice.test.client.builder.call.RestCallAnalyzer;
import ru.vyarus.dropwizard.guice.url.model.ResourceMethodInfo;
import ru.vyarus.dropwizard.guice.url.resource.ResourceAnalyzer;
import ru.vyarus.dropwizard.guice.url.resource.ResourceParamsBuilder;
import ru.vyarus.dropwizard.guice.url.util.Caller;

import java.lang.reflect.Method;

/**
 * Resource path builder. Used to build rest path using resource class and methods.
 * <p>
 * Sub resources could be specified with {@link #subResource(String, Class, Object...)}.
 * <p>
 * Path parameters and additional query parameters could be resolved with {@link #pathParam(String, Object)} and
 * {@link #queryParam(String, Object...)}. Note that when resource path is built from method, required path and
 * query parameters could be specified as resource methods arguments
 * ({@link #method(ru.vyarus.dropwizard.guice.url.util.Caller)}).
 * <p>
 * Final path could be built either as template (preserving path parameters) {@link #buildTemplate()} or as
 * final path with resolved path parameters {@link #build()}.
 *
 * @param <T> resource type
 * @author Vyacheslav Rusakov
 * @see ru.vyarus.dropwizard.guice.url.util.RestPathUtils for direct resource path resolution
 * @since 26.09.2025
 */
public class RestPathBuilder<T> extends ResourceParamsBuilder {

    private final Class<T> resource;

    /**
     * Create a builder.
     * <p>
     * Note that jersey ignores {@link jakarta.ws.rs.Path} annotation for sub resources (only lookup method path
     * is used)
     *
     * @param basePath optional base path
     * @param resource target resource
     * @param subResource true if it is a sub resource (important because {@code @Path} annotation must be ignored)
     */
    public RestPathBuilder(final @Nullable String basePath, final Class<T> resource, final boolean subResource) {
        super(basePath);
        this.resource = resource;
        // the same builder could be used for both resources and sub resources, so have to allow resources
        // without root @Path annotation here
        final String resourcePath = subResource ? null : ResourceAnalyzer.getResourcePath(resource);
        if (resourcePath != null) {
            builder.path(resourcePath);
        }
    }

    /**
     * Append a sub-resource path to the current resource path (with sub-resource method resolution).
     * <p>
     * Sub-resource path is declared by a sub-resource method: {@code @Path("/sub") SubResource something() {...}}.
     * Here sub-resource path would include path from declaration method and path, declared for sub-resource class.
     * The current method does not search for tareget declaration method (in some cases it is impossible), so
     * sub-resource mapping path (from method) must be applied manually.
     *
     * @param path        sub-resource mapping path (from sub-resource method; could contain String.format()
     *                    placeholders: %s)
     * @param subResource sub-resource
     * @param args        args variables for path placeholders (String.format() arguments)
     * @param <K>         sub-resource type
     * @return builder for sub-resource
     */
    public <K> RestPathBuilder<K> subResource(final String path, final Class<K> subResource, final Object... args) {
        builder.path(String.format(path, args));
        final RestPathBuilder<K> sub = new RestPathBuilder<>(builder.toString(), subResource, true);
        sub.pathParams.putAll(pathParams);
        return sub;
    }

    /**
     * Append a sub-resource path to the current resource path. Sub resource path is resolved through the called
     * locator method.
     * <p>
     * Note: multiple locator methods could be called at once!
     *
     * @param caller      locator method(s) caller
     * @param subResource sub-resource type
     * @param <K>         sub-resource type
     * @return builder for sub-resource
     */
    public <K> RestPathBuilder<K> subResource(final Caller<T> caller, final Class<K> subResource) {
        final String path = RestCallAnalyzer.getSubResourcePath(resource, caller);
        builder.path(path);
        final RestPathBuilder<K> sub = new RestPathBuilder<>(builder.toString(), subResource, true);
        sub.pathParams.putAll(pathParams);
        return sub;
    }

    /**
     * Append provided path to resource path (useful when direct resource method can't be used for construction).
     * <p>
     * Query parameters could be specified directly.
     *
     * @param path target path, relative to resource root  (could contain String.format() placeholders: %s)
     * @param args variables for path placeholders (String.format() arguments)
     * @return parameters builder to specify path and query parameters
     * @see #method(ru.vyarus.dropwizard.guice.url.util.Caller)
     */
    public ResourceParamsBuilder path(final String path, final Object... args) {
        applyPath(path, args);
        return this;
    }

    /**
     * Append resource method path (path from {@link jakarta.ws.rs.Path} method annotation).
     * <p>
     * When parameters are not provided - search for any method with name. If multiple methods
     * with the same name would be found - method with no parameters would be selected, otherwise exception thrown.
     *
     * @param methodName method name
     * @param parameters method argument types
     * @return parameters builder to specify path and query parameters
     * @throws java.lang.IllegalStateException if no unique method found for provided name
     * @see #method(ru.vyarus.dropwizard.guice.url.util.Caller)
     */
    public ResourceParamsBuilder method(final String methodName, final Class<?>... parameters) {
        if (parameters.length == 0) {
            builder.path(ResourceAnalyzer.getMethodPath(resource, methodName));
            return this;
        } else {
            return method(ResourceAnalyzer.findMethod(resource, methodName, parameters));
        }
    }

    /**
     * Append resource method path (path from {@link jakarta.ws.rs.Path} method annotation).
     *
     * @param method resource method
     * @return parameters builder to specify path and query parameters
     * @see #method(ru.vyarus.dropwizard.guice.url.util.Caller)
     */
    public ResourceParamsBuilder method(final Method method) {
        ResourceAnalyzer.validateResourceMethod(resource, method);
        builder.path(ResourceAnalyzer.getMethodPath(method));
        return this;
    }

    /**
     * Append resource method path (path from {@link jakarta.ws.rs.Path} method annotation).
     * Also, use path and query providers, provided as method arguments. Note that only non-null parameters counted.
     * <p>
     * Might also include sub resource call, if sub resource locator method returns resource instance:
     * {@code resource.sub(args).method(args)}
     *
     * @param caller consumer with exactly one resource method execution
     * @return parameters builder to specify path and query parameters
     */
    public ResourceParamsBuilder method(final Caller<T> caller) {
        final ResourceMethodInfo info = ResourceAnalyzer.analyzeMethodCall(resource, caller);
        builder.path(info.getPath());
        pathParams.putAll(info.getPathParams());
        info.getQueryParams().forEach(builder::queryParam);
        info.getMatrixParams().forEach(builder::matrixParam);
        return this;
    }
}
