package ru.vyarus.dropwizard.guice.test.client;

import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.UriBuilder;
import org.jspecify.annotations.Nullable;
import ru.vyarus.dropwizard.guice.test.client.builder.TestClientRequestBuilder;
import ru.vyarus.dropwizard.guice.test.client.builder.TestRequestConfig;
import ru.vyarus.dropwizard.guice.test.client.builder.call.MultipartAwareCaller;
import ru.vyarus.dropwizard.guice.test.client.builder.call.MultipartArgumentHelper;
import ru.vyarus.dropwizard.guice.test.client.builder.call.RestCallAnalyzer;
import ru.vyarus.dropwizard.guice.url.resource.ResourceAnalyzer;
import ru.vyarus.dropwizard.guice.url.util.Caller;

import java.lang.reflect.Method;
import java.util.function.Supplier;

/**
 * Specialized rest client for resource classes. Useful to construct target paths directly from resource class methods.
 * <p>
 * For example: {@code client.method(mock -> mock.restMethod("queryParam").asVoid()))}
 * It would resolve target HTTP method, target path (from method annotation) and apply query parameter
 * (because of not null value provided for method parameter, annotated with {@link jakarta.ws.rs.QueryParam}).
 * <p>
 * This simplifies test request building, making it almost refactoring-safely. Also, it makes simple navigation from
 * test to called resource method.
 * <p>
 * It is still possible to call methods by path with generic methods like {@link #get(String, Class, Object...)}
 * or {@link #buildGet(String, Object...)}.
 * <p>
 * {@inheritDoc}
 *
 * @param <T> resource type
 * @author Vyacheslav Rusakov
 * @since 18.09.2025
 */
public class ResourceClient<T> extends TestRestClient<TestRestClient<?>> {

    private final Class<T> resource;

    /**
     * Create a resource client.
     *
     * @param root     root path
     * @param defaults defaults
     * @param resource resource type
     */
    public ResourceClient(final @Nullable Supplier<WebTarget> root,
                          final @Nullable TestRequestConfig defaults,
                          final Class<T> resource) {
        super(root, defaults);
        this.resource = resource;
    }

    /**
     * The same as {@link #method(ru.vyarus.dropwizard.guice.url.util.Caller, Object)}, but provides a helper
     * utility to easily stub multipart parameters (so these values could be used for request configuration).
     * <p>
     * For the most common case {@code post(@FormDataParam("file") InputStream stream,
     *
     * @param consumer consumer calling resource method
     * @param caller   multipart method caller
     * @return pre-configured request builder instance
     * @return builder instance for chained calls
     * @FormDataParam("file") FormDataContentDisposition fileDetail)}:
     * <pre>{@code multipartMethod((instance, multipart) -> instance
     *             .post(multipart.fromClasspath("/some.txt"),
     *                   multipart.disposition("file", "some.txt"))}</pre>.
     * <p>
     * it could be a single field: {@code post(@FormDataParam("file") FormDataBodyPart file)}:
     * <pre>{@code multipartMethod((instance, multipart) -> instance
     *             .post(multipart.filePart("/some.txt"))}</pre>
     * or to get file from classpath:
     * <pre>{@code multipartMethod((instance, multipart) -> instance
     *             .post(multipart.streamPart("/some.txt"))}</pre>.
     * <p>
     * There might be multiple files for the same field
     * {@code post(@FormDataParam("file") List<FormDataBodyPart> file)}:
     * <pre>{@code multipartMethod((instance, multipart) -> instance
     *          .post(Arrays.asList(
     *              multipart.filePart("/some.txt"),
     *              multipart.filePart("/other.txt")))}</pre>.
     * <p>
     * When the method only accepts content-disposition mapping, it would be also used with en empty file content
     * (as file content is not required, preserve available data):
     * {@code post(@FormDataParam("file") FormDataContentDisposition file)}
     * <pre>{@code multipartMethod((instance, multipart) -> instance
     *           .post(multipart.disposition("file", "some.txt"))}</pre>.
     * <p>
     * The method parameter could be a complete multipart object (with all fields inside):
     * {@code post(FormDataMultiPart multiPart)}: for such cases there is a special builder:
     * <pre>{@code multipartMethod((instance, multipart) -> instance
     *           .post(multipart.multipart()
     *                  .field("foo", "val")
     *                  .file("file1", "/some.txt)
     *                  .stream("file2", "/other.txt)
     *                  .build())}</pre>
     * <p>
     * It is not required to specify all parameters: you may use null on any of them (it is just a one way to
     * configure request).
     * <p>
     * Note that it is not required to declare fields like this! You can always prepare entity manually and use
     * caller with body {@link #method(ru.vyarus.dropwizard.guice.url.util.Caller, Object)}.
     *
     * @param caller multipart method caller
     * @return builder instance for chained calls
     */
    public TestClientRequestBuilder multipartMethod(final MultipartAwareCaller<T> caller) {
        return this.method(instance -> caller.call(instance, new MultipartArgumentHelper()));
    }

    /**
     * Configure request from resource method call. See
     * {@link #method(ru.vyarus.dropwizard.guice.url.util.Caller, Object)}.
     *
     * @param caller resource method caller
     * @return pre-configured request builder instance
     */
    public TestClientRequestBuilder method(final Caller<T> caller) {
        return method(caller, null);
    }

    /**
     * Configure a request from the resource method call. The main idea here: request could be configured
     * automatically from the resource method annotations. Arguments for annotated parameters could be used to
     * provide default values.
     * <ul>
     *  <li>target path resolved from method {@link jakarta.ws.rs.Path}</li>
     *  <li>target method resolved from method annotation (like {@link jakarta.ws.rs.GET})</li>
     *  <li>analyze provided method arguments to get values for annotated parameters (like
     *  {@link jakarta.ws.rs.QueryParam}, {@link jakarta.ws.rs.PathParam}, etc.)</li>
     *  <li>build form body from not null {@link jakarta.ws.rs.FormParam} and
     *  {@link org.glassfish.jersey.media.multipart.FormDataParam}</li>
     * </ul>
     * <p>
     * All these configuration values could be overridden with a received builder. Also, put null on arguments
     * that should not be configured automatically.
     * <p>
     * Note that by default requests are logged, so the correctness of the target url could be verified easily.
     * <p>
     * Might also include sub resource call, if sub resource locator method returns resource instance:
     * {@code resource.sub(args).method(args)}
     *
     * @param caller resource method caller
     * @param body   request body (form param ignored for POST if body provided)
     * @return pre-configured request builder instance
     */
    public TestClientRequestBuilder method(final Caller<T> caller, final @Nullable Object body) {
        return RestCallAnalyzer.configure(this, caller, body);
    }

    /**
     * Create request builder from resource method: automatic configuration of targe path and http method selection.
     * <p>
     * For more advanced defaults configuration see {@link #method(ru.vyarus.dropwizard.guice.url.util.Caller)}
     * (it will also apply configuration by provided method parameter values).
     * <p>
     * If multiple methods selected, will select method
     * without arguments. If there is no matching no-args method found throws exception (about multiple methods found).
     * This no-args behavior is required to comply with arguments-based search (it would be otherwise impossible
     * to search for no-args method).
     *
     * @param method method name to call (there must be only one such method)
     * @return request builder instance
     * @throws java.lang.IllegalStateException if unique method not found
     */
    public TestClientRequestBuilder method(final String method) {
        return method(method, null);
    }

    /**
     * Create request builder from resource method: automatic configuration of targe path and http method selection.
     * <p>
     * For more advanced defaults configuration see {@link #method(ru.vyarus.dropwizard.guice.url.util.Caller)}
     * (it will also apply configuration by provided method parameter values).
     *
     * @param method method name to call (there must be only one such method)
     * @return request builder instance
     */
    public TestClientRequestBuilder method(final Method method) {
        return method(method, null);
    }

    /**
     * Create request builder from resource method: automatic configuration of targe path and http method selection.
     * <p>
     * For more advanced defaults configuration see {@link #method(ru.vyarus.dropwizard.guice.url.util.Caller, Object)}
     * (it will also apply configuration by provided method parameter values).
     *
     * @param method method name to call (there must be only one such method)
     * @param body   (optional) request body (everything except {@link jakarta.ws.rs.client.Entity} converted to JSON)
     * @return request builder instance
     * @throws java.lang.IllegalStateException if jersey annotations aren't found on the method
     */
    public TestClientRequestBuilder method(final Method method, final @Nullable Object body) {
        ResourceAnalyzer.validateResourceMethod(resource, method);
        final Method annotated = ResourceAnalyzer.findAnnotatedMethod(method);
        final UriBuilder builder = UriBuilder.newInstance();
        builder.path(annotated);

        final String httpMethod = ResourceAnalyzer.findHttpMethod(annotated);
        return build(httpMethod, builder.toTemplate(), body);
    }

    /**
     * Create request builder from resource method: automatic configuration of targe path and http method selection.
     * <p>
     * For more advanced defaults configuration see {@link #method(ru.vyarus.dropwizard.guice.url.util.Caller, Object)}
     * (it will also apply configuration by provided method parameter values).
     * <p>
     * If multiple methods selected, will select the method without arguments. If there is no matching no-args method
     * found, throws exception (about multiple methods found).
     *
     * @param method method name to call (there must be only one such method)
     * @param body   (optional) request body (everything except {@link jakarta.ws.rs.client.Entity} converted to JSON)
     * @return request builder instance
     * @throws java.lang.IllegalStateException if unique method not found
     */
    public TestClientRequestBuilder method(final String method, final @Nullable Object body) {
        final Method target = ResourceAnalyzer.findMethod(resource, method);
        return method(target, body);
    }

    @Override
    public <R> ResourceClient<R> subClient(final Class<R> resource) {
        // to minimize silly mistakes
        throw new UnsupportedOperationException("In context of resource, sub-resource client should be obtained "
                + "with subResourceClient() method which ignores sub-resource @Path annotation (not used in "
                + "sub-resource path building)");
    }

    /**
     * Create a sub client for the sub-resource. Sub resource path is resolved through the called locator method.
     * Note: multiple locator methods could be called at once!
     *
     * @param caller      locator method(s) caller
     * @param subResource sub-resource type
     * @param <R>         sub-resource type
     * @return sub-resource client
     */
    public <R> ResourceClient<R> subResourceClient(final Caller<T> caller, final Class<R> subResource) {
        final String path = RestCallAnalyzer.getSubResourcePath(getResourceType(), caller);
        // last class used for a resource type to get methods on
        return new ResourceClient<>(() -> target(path), defaults, subResource);
    }

    /**
     * @return resource type
     */
    public Class<T> getResourceType() {
        return resource;
    }

    @Override
    public String toString() {
        return "Rest client for: " + resource.getSimpleName() + " (" + getRoot().getUri().toString() + ")";
    }
}
