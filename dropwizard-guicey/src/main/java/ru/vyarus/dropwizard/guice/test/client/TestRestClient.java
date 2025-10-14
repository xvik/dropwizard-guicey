package ru.vyarus.dropwizard.guice.test.client;

import jakarta.ws.rs.client.WebTarget;
import org.jspecify.annotations.Nullable;
import ru.vyarus.dropwizard.guice.test.client.builder.TestRequestConfig;
import ru.vyarus.dropwizard.guice.url.util.RestPathUtils;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Adds rest-specific methods for sub-clients creation:
 * <ul>
 *     <li>{@link #subClient(Class)} - build path automatically using class declaration</li>
 *     <li>{@link #subClient(Consumer)} - build target path with {@link jakarta.ws.rs.core.UriBuilder}</li>
 * </ul>
 * <p>
 * Resource-class-based clients are refactoring-friendly. Moreover, it becomes super simple to correlate
 * a client call with the target resource class. Also, such clients provide an ability to use real resource method
 * for path declaration.
 * <p>
 * {@inheritDoc}
 *
 * @author Vyacheslav Rusakov
 * @since 16.09.2025
 * @param <T> actual client type
 */
public class TestRestClient<T extends TestRestClient<?>> extends TestClient<T> {

    /**
     * Special case: used by stubs rest client {@link ru.vyarus.dropwizard.guice.test.rest.RestClient}.
     */
    public TestRestClient() {
        this(null, null);
    }

    /**
     * Construct a rest client.
     *
     * @param root target supplier
     * @param defaults (optional) defaults
     */
    public TestRestClient(final @Nullable Supplier<WebTarget> root, final @Nullable TestRequestConfig defaults) {
        super(root, defaults);
    }

    /**
     * Create a new sub-client for a specified resource class (appends a resource path, obtained from
     * {@link jakarta.ws.rs.Path} annotation, to the current client path). Method is useful when generic
     * rest path must be "typed" with a resource type (to be able to call resource methods directly).
     * <p>
     * In case of sub-resources, use {@link #subResourceClient(String, Class, Object...)} to properly specify
     * sub-resource mapping path (from lookup method):
     * {@code ResourceClient rest = client.subResourceClient("path", SubResource.class)}.
     * IMPORTANT: this is NOT THE SAME: {@code client.subClient("path").subClient(SubResource.class)} because
     * "subClient()" call would append path from resource, which is ignored for sub resources!.
     * <p>
     * Defaults could be used to declare path parameter values:
     * {@code ResourceClient rest = client.subClient(Resource.class).defaultPathParam("param", "value")} where
     * a resource class path is like "/some/{param}/path". With the default path param, there would be no need to
     * declare it for each request call.
     * <p>
     * All defaults, configured for the current client, will be inherited in a sub-client. If this is not required,
     * just clean defaults after creation: {@code client.subClient(ResClass.class).reset()}.
     *
     * @param resource resource class one to build a path for
     * @return resource client (with a resource path, relative to the current client path)
     * @param <R> resource type
     */
    public <R> ResourceClient<R> subClient(final Class<R> resource) {
        final String target = RestPathUtils.getResourcePath(resource);
        // last class used for a resource type to get methods on
        return new ResourceClient<>(() -> target(target), defaults, resource);
    }

    /**
     * Create a sub client for the sub-resource.
     * <p>
     * IMPORTANT: Path, declared on sub-resource class is ignored! Only lookup method path is counted.
     * For example, {@code @Path("/sub") SubResource something() {...}} means all sub resource methods would be
     * available on "/sub/*".
     *
     * @param path        sub-resource mapping path (from sub-resource method; could contain String.format()
     *                    placeholders: %s)
     * @param args variables for path placeholders (String.format() arguments)
     * @param subResource sub-resource
     * @param <R>         sub-resource type
     * @return sub-resource client
     */
    public <R> ResourceClient<R> subResourceClient(final String path, final Class<R> subResource,
                                                   final Object... args) {
        final String target = String.format(path, args);
        // last class used for a resource type to get methods on
        return new ResourceClient<>(() -> target(target), defaults, subResource);
    }


    @Override
    public String toString() {
        return "Rest client for: " + getRoot().getUri().toString();
    }

    @Override
    @SuppressWarnings("unchecked")
    protected T createClient(final String target) {
        return (T) new TestRestClient<>(() -> target(target), defaults);
    }
}
