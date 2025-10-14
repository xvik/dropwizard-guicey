package ru.vyarus.dropwizard.guice.test.client;

import com.google.common.base.Preconditions;
import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.UriBuilder;
import org.jspecify.annotations.Nullable;
import ru.vyarus.dropwizard.guice.test.client.builder.FormBuilder;
import ru.vyarus.dropwizard.guice.test.client.builder.TestClientDefaults;
import ru.vyarus.dropwizard.guice.test.client.builder.TestClientRequestBuilder;
import ru.vyarus.dropwizard.guice.test.client.builder.TestRequestConfig;

import java.net.URI;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Wrapper for {@link org.glassfish.jersey.client.JerseyClient}. Jersey client is a general-purpose client and
 * this api specialized for tests. Also, the jersey request builder is split into two parts: target and request.
 * This api unifies it into a single step, for simplicity.
 * <p>
 * The "defaults" concept: some values (query params, headers, cookies, etc.) could be configured once
 * for all client requests (this is usually handy for tests, for example, to unify authorization).
 * See "default*" methods for details (for example, {@link #defaultHeader(String, Object)}).
 * Defaults could be cleared with {@link #reset()} or printed into console with {@link #printDefaults()}
 * <p>
 * There are multiple ways to perform a request:
 * <ul>
 *     <li>Raw jersey target {@link #target(String, Object...)} (defaults does not apply!)</li>
 *     <li>Raw jersey builder {@link #request(String, Object...)} with applied defaults</li>
 *     <li>Shortcut methods (to execute a general call and receive the result) like
 *     {@link #get(String, Class, Object...)}, {@link #post(String, Object, Class, Object...)}, etc.</li>
 *     <li>Shortcut builders (for additional request configurations) like {@link #buildGet(String, Object...)},
 *     {@link #buildPost(String, Object, Object...)}, etc</li>
 *     <li>Special builder for forms (multipart and urlencoded): {@link #buildForm(String, Object...)}</li>
 *     <li>General builder: {@link #build(String, String, Object, Object...)}</li>
 * </ul>
 * <p>
 * All "build*" methods return a custom builder object with all the same configuration options as the original
 * jersey client builder, but all gathered in one place (easier to use). This builder could return either
 * directly mapped object (with
 * {@link TestClientRequestBuilder#as(Class)}) or a special response object wrapper with pre-defined assertions
 * to simplify testing ({@link TestClientRequestBuilder#expectSuccess(Integer...)} or
 * {@link TestClientRequestBuilder#expectFailure(Integer...)} or without response validation
 * {@link TestClientRequestBuilder#invoke()}).
 * <p>
 * All methods support (optional) string format for a path. For example, the call
 * {@code client.get("/entity/%s", Entity.class, 12)} will use "/entity/12/" as target path.
 * <p>
 * Examples:
 * <pre><code>
 *     TestClient client;
 *
 *     // jersey api (no defaults applied)
 *     Entity res = client.target("/some/path").request().get(Entity.class);
 *
 *     // jersey api (with defaults applied)
 *     Entity res = client.request("/some/path").get(Entity.class);
 *
 *     // shortcut method
 *     Entity res = client.get("/some/path", Entity.class);
 *
 *     // shortcut with composite entity type (note diamond operator usage):
 *     List&lt;Entity&gt; res = client.get("/some/path", new GenericType&lt;&gt;() {});
 *
 *     // builder method
 *     Entity res = client.buildGet("/some/path")
 *              .header("Something", "Value")
 *              .invoke(Entity.class);
 *
 *     // builder with response assertions ():
 *     Entity res = client.buildGet("/some/path")
 *              // error thrown if response is not success with (optional) exact status validation
 *              .expectSuccess(200)
 *              // assertion error if value will not match or cookie absent
 *              .assertCookie("Name", "Value")
 *              // assertion error if value will not match or header absent
 *              .assertHeader("Something", "Value")
 *              // map entity
 *              .as(EntityClass)
 *
 *     // defaults demo:
 *     client.defaultHeader("Authorization", "Bearer 1234567890");
 *
 *     // request would contain the default header
 *     Entity res = client.get("/some/path", Entity.class);
 * </code></pre>
 * <p>
 * The main idea behind chained assertions is redundant variables avoidance in test.
 * <p>
 * Note that builder method return {@link java.lang.AutoCloseable} object (same as {@link jakarta.ws.rs.core.Response})
 * because a response object must be closed (response is closed when response body is consumed).
 * Because of this your IDE could warn you that the result of, for example, {@link TestClientRequestBuilder#invoke()}
 * must be used with "try-with-resources" statement. In most cases, you can ignore this warning as guicey tracks all
 * such wrapped responses and would close it after a test application shutdown.
 * <p>
 * Builder api does not hide jersey api: if required, you can modify target directly with
 * {@link #defaultPathConfiguration(java.util.function.Function)} or jersey builder with
 * {@link #defaultRequestConfiguration(java.util.function.Consumer)}.
 * <p>
 * Builder supports jersey client properties ({@link #defaultProperty(String, Object)}) and extensions
 * {@link #defaultRegister(Class)} and {@link #defaultRegister(Object)}. As an example, property could be used to
 * disable redirects ({@link TestClientRequestBuilder#notFollowRedirects()} which is used automatically by
 * {@link TestClientRequestBuilder#expectRedirect(Integer...)}). For a custom extension example see
 * {@link TestClientRequestBuilder#noBodyMappingForVoid()} (which is used automatically by
 * {@link TestClientRequestBuilder#asVoid()} to ignore response body mapping for void requests).
 * <p>
 * The client could be used to build sub-clients. In this case all defaults will be inherited from the parent client.
 * For example, we have a general rest client, but our particular test would check only one resource. In this case
 * we can create a sub-client for the resource path to avoid it in all method calls:
 * <pre><code>
 *     TestClient api;
 *     TestClient subRest = api.subClient("/resource/{param}/path")
 *              .defaultPathParam("param", 123);
 *      // this would call "/resource/123/path/method"
 *     Entity res = subRest.get("/method", Entity.class);
 * </code></pre>
 * <p>
 * {@link TestClient} is a general client class, but there are special client classes for rest (extending it):
 * {@link ru.vyarus.dropwizard.guice.test.client.TestRestClient} and
 * {@link ru.vyarus.dropwizard.guice.test.client.ResourceClient} (they could be obtained from the root
 * {@link ru.vyarus.dropwizard.guice.test.ClientSupport object (which is also a test client)})
 *
 * @param <T> actual client type
 * @author Vyacheslav Rusakov
 * @since 13.09.2025
 */
@SuppressWarnings({"PMD.CouplingBetweenObjects", "PMD.TooManyMethods"})
public class TestClient<T extends TestClient<?>> extends TestClientDefaults<T> {

    private final Supplier<WebTarget> root;

    /**
     * Construct a client without a base root path and defaults. This constructor could be used ONLY by
     * extending classes, which override {@link #getRoot()} (these are root classes providing initial integration
     * with test or stub clients ({@link ru.vyarus.dropwizard.guice.test.ClientSupport} and
     * {@link ru.vyarus.dropwizard.guice.test.rest.RestClient}) because it is not possible to provide
     * root target in constructor)
     *
     * @param defaults default configurations
     */
    public TestClient(@Nullable final TestRequestConfig defaults) {
        this(null, defaults);
    }

    /**
     * Construct a jersey client wrapper.
     *
     * @param root     root target supplier
     * @param defaults (optional) defaults
     */
    public TestClient(final @Nullable Supplier<WebTarget> root, final @Nullable TestRequestConfig defaults) {
        super(new TestRequestConfig(defaults));
        Preconditions.checkState(root != null || !getClass().equals(TestClient.class),
                "Target supplier may not be null for direct TestClient object usage: it could be null only for "
                        + "classes, extending TestClient (because they override getRoot() method)");
        this.root = root;
    }

    /**
     * @return base URI for this client
     */
    public URI getBaseUri() {
        return getRoot().getUri();
    }

    // ------------------------------------------------------------------------ PURE JERSEY

    /**
     * Creates a web target to call under testing. This is a pure jersey api method for generic cases: in most cases,
     * it would be simplir to use provided shortcuts (like {@link #get(String, Object...)}).
     * <p>
     * Example: {@code .target("/smth/12/other").request().buildGet().invoke()}
     * String format: {@code .target("/smth/%s/other", 12).request().buildGet().invoke()}
     * <p>
     * WARNING: any specified defaults do not affect this method!
     *
     * @param path target path, relative to client root (could contain String.format() placeholders: %s)
     * @param args variables for path placeholders (String.format() arguments)
     * @return jersey web target object
     */
    public WebTarget target(final String path, final Object... args) {
        final String target = path == null || path.isEmpty() ? "" : String.format(path, args);
        return getRoot().path(target);
    }

    /**
     * Create request for provided target with all defaults applied. Use for generic cases when jersey api
     * usage required. In other cases use provided shortcuts or builder methods.
     *
     * @param path target path, relative to client root  (could contain String.format() placeholders: %s)
     * @param args variables for path placeholders (String.format() arguments)
     * @return request object, ready to be sent
     */
    public Invocation.Builder request(final String path, final Object... args) {
        return defaults.applyRequestConfiguration(target(path, args));
    }

    // ------------------------------------------------------------------------ SUB CLIENTS

    /**
     * Construct a sub-client with the provided path. Useful when performing multiple tests for common base url
     * (e.g. testing one resource methods).
     * <pre><code>
     *    TestClient api;
     *    TestClient subRest = api.subClient("/resource/{param}/path")
     *             .defaultPathParam("param", 123);
     *    // this would call "/resource/123/path/method"
     *    Entity res = subRest.get("/method", Entity.class);
     * </code></pre>
     * <p>
     * All defaults, configured for the current client, will be inherited in a sub-client. If this is not required,
     * just clean defaults after creation: {@code client.subClient("path").reset()}.
     *
     * @param path target client root path  (could contain String.format() placeholders: %s)
     * @param args variables for path placeholders (String.format() arguments)
     * @return new client with a different root path
     */
    public T subClient(final String path, final Object... args) {
        Preconditions.checkState(!path.toLowerCase().startsWith("http"),
                "Only sub urls relative to current client url could be used. For completely custom external "
                        + "client creation use ClientSupport.customClient()");
        // client INHERITS current defaults
        return createClient(String.format(path, args));
    }

    /**
     * Create a new sub-client for a specified path. Sub path is constructed using jersey builder.
     * For example: {@code client.subClient(builder -> builder.path("path").path("sub"))}.
     * <p>
     * This might be useful for constructing complex paths with matrix parameters inside. Note that the target client
     * could be easily "typed" with {@link #asRestClient(Class)} method. Or use {@link #subClient(String, Object...)}.
     * <p>
     * All defaults, configured for the current client, will be inherited in a sub-client. If this is not required,
     * just clean defaults after creation: {@code client.subClient(...).reset()}.
     *
     * @param consumer uri builder configurator
     * @return client with a constructed path (relative to the current client path)
     */
    public T subClient(final Consumer<UriBuilder> consumer) {
        final UriBuilder uriBuilder = UriBuilder.newInstance();
        consumer.accept(uriBuilder);
        return createClient(uriBuilder.toString());
    }

    /**
     * Create a new rest client with a custom path.
     *
     * @param consumer path builder
     * @param resource resource type
     * @param <K>      resource type
     * @return rest client for provided resource
     */
    public <K> ResourceClient<K> subClient(final Consumer<UriBuilder> consumer, final Class<K> resource) {
        final UriBuilder uriBuilder = UriBuilder.newInstance();
        consumer.accept(uriBuilder);
        return new ResourceClient<>(() -> target(uriBuilder.toString()), defaults, resource);
    }

    /**
     * Cast current path as provided resource (full!) path. Use-case: resources were mapped on non-standard path
     * (admin context resources or internal resource mappings).
     * <p>
     * IMPORTANT: this call will ignore resource {@code @Path} annotation - it assumes that the current path is
     * already a resource path.
     *
     * @param resource resource type
     * @param <T>      resource type
     * @return rest client for provided resource
     */
    public <T> ResourceClient<T> asRestClient(final Class<T> resource) {
        return new ResourceClient<>(() -> target("/"), defaults, resource);
    }

    // ------------------------------------------------------------------------ REQUEST SHORTCUTS

    /**
     * GET call shortcut. Almost the same as jersey {@code client.target(path).request().get(Void.class)}:
     * <p>
     * To send a form (urlencoded) use {@link #buildForm(String, Object...)}:
     * {@code buildForm("/path/%s/sub", 12).param("A", 1)..buildGet().invoke(Some.class)}.
     * <ul>
     *    <li>Exception thrown if the result is not successful (not 2xx)</li>
     *    <li>For {@link ru.vyarus.dropwizard.guice.test.jupiter.ext.rest.StubRest} resource exception
     *    propagated if no exception mappers registered</li>
     *    <li>Response body is ignored ({@link TestClientRequestBuilder#noBodyMappingForVoid()})</li>
     * </ul>
     * <p>
     * Additional headers, query params, etc. could be provided with defaults
     * (e.g. {@link #defaultHeader(String, Object)}). For additional request-specific configuration
     * use {@link #buildGet(String, Object...)}.
     * <p>
     * String format could be used for path formatting:
     * {@code client.get("/smth/%s/other", 12)}
     *
     * @param path target path, relative to rest root  (could contain String.format() placeholders: %s)
     * @param args variables for path placeholders (String.format() arguments)
     */
    public void get(final String path, final Object... args) {
        get(path, Void.class, args);
    }

    /**
     * GET call shortcut. Almost the same as jersey {@code client.target(path).request().get(Some.class)}:
     * <p>
     * To send a form (urlencoded) use {@link #buildForm(String, Object...)}:
     * {@code buildForm("/path/%s/sub", 12).param("A", 1)..buildGet().invoke(Some.class)}.
     * <ul>
     *    <li>Exception thrown if the result is not successful (not 2xx)</li>
     *    <li>For {@link ru.vyarus.dropwizard.guice.test.jupiter.ext.rest.StubRest} resource exception
     *    propagated if no exception mappers registered</li>
     * </ul>
     * <p>
     * Additional headers, query params, etc. could be provided with defaults
     * (e.g. {@link #defaultHeader(String, Object)}). For additional request-specific configuration
     * use {@link #buildGet(String, Object...)}.
     * <p>
     * String format could be used for path formatting:
     * {@code Some res = client.get("/smth/%s/other", Some.class, 12)}
     *
     * @param path   target path, relative to rest root (could contain String.format() placeholders: %s)
     * @param result result type (null or Void to ignore response body)
     * @param args   variables for path placeholders (String.format() arguments)
     * @param <R>    result type
     * @return mapped result object or null (if class not declared)
     */
    public <R> R get(final String path, final @Nullable Class<R> result, final Object... args) {
        return handleShortcut(buildGet(path, args), result);
    }

    /**
     * GET call shortcut. Almost the same as jersey
     * {@code client.target(path).request().get(new GenericType<List<Some>>(){})}:
     * <p>
     * To send a form (urlencoded) use {@link #buildForm(String, Object...)}:
     * {@code buildForm("/path/%s/sub", 12).param("A", 1)..buildGet().invoke(Some.class)}.
     * <ul>
     *    <li>Exception thrown if the result is not successful (not 2xx)</li>
     *    <li>For {@link ru.vyarus.dropwizard.guice.test.jupiter.ext.rest.StubRest} resource exception
     *    propagated if no exception mappers registered</li>
     * </ul>
     * <p>
     * Additional headers, query params, etc. could be provided with defaults
     * (e.g. {@link #defaultHeader(String, Object)}). For additional request-specific configuration
     * use {@link #buildGet(String, Object...)}.
     * <p>
     * String format could be used for path formatting:
     * {@code List<Some> res = client.get("/smth/%s/other", new GenericType<>() {}, 12)}
     *
     * @param path   target path, relative to rest root (could contain String.format() placeholders: %s)
     * @param result result type (null or Void to ignore response body)
     * @param args   variables for path placeholders (String.format() arguments)
     * @param <R>    result type
     * @return mapped result object or null (if class not declared)
     */
    public <R> R get(final String path, final @Nullable GenericType<R> result, final Object... args) {
        return handleShortcut(buildGet(path, args), result);
    }

    /**
     * POST call shortcut. Almost the same as jersey {@code client.target(path).request().post(entity, Void.class))}:
     * <p>
     * If body is already an {@link jakarta.ws.rs.client.Entity} - it will be used as is, other objects
     * would be converted to json entity ({@link Entity#json(Object)}).
     * <p>
     * For forms (urlencoded and multipart) use {@link #buildForm(String, Object...)}:
     * {@code buildForm("/path/%s/sub", 12).buildPost().invoke(Some.class)}.
     * <ul>
     *    <li>Exception thrown if the result is not successful (not 2xx)</li>
     *    <li>For {@link ru.vyarus.dropwizard.guice.test.jupiter.ext.rest.StubRest} resource exception
     *    propagated if no exception mappers registered</li>
     *    <li>Response body is ignored ({@link TestClientRequestBuilder#noBodyMappingForVoid()})</li>
     * </ul>
     * <p>
     * Additional headers, query params, etc. could be provided with defaults
     * (e.g. {@link #defaultHeader(String, Object)}). For additional request-specific configuration
     * use {@link #buildPost(String, Object, Object...)}.
     * <p>
     * String format could be used for path formatting:
     * {@code client.post("/smth/%s/other", object, 12)}
     *
     * @param path target path, relative to rest root (could contain String.format() placeholders: %s)
     * @param body entity object (everything except {@link Entity} converted to JSON)
     * @param args variables for path placeholders (String.format() arguments)
     */
    public void post(final String path, final @Nullable Object body, final Object... args) {
        post(path, body, Void.class, args);
    }

    /**
     * POST call shortcut. Almost the same as jersey {@code client.target(path).request().post(entity, Some.class))}:
     * <p>
     * If body is already an {@link jakarta.ws.rs.client.Entity} - it will be used as is, other objects
     * would be converted to json entity ({@link Entity#json(Object)}).
     * <p>
     * For forms (urlencoded and multipart) use {@link #buildForm(String, Object...)}:
     * {@code buildForm("/path/%s/sub", 12).param("A", 1)..buildPost().invoke(Some.class)}.
     * <ul>
     *    <li>Exception thrown if the result is not successful (not 2xx)</li>
     *    <li>For {@link ru.vyarus.dropwizard.guice.test.jupiter.ext.rest.StubRest} resource exception
     *    propagated if no exception mappers registered</li>
     * </ul>
     * <p>
     * Additional headers, query params, etc. could be provided with defaults
     * (e.g. {@link #defaultHeader(String, Object)}). For additional request-specific configuration
     * use {@link #buildPost(String, Object, Object...)}.
     * <p>
     * String format could be used for path formatting:
     * {@code Some res = client.post("/smth/%s/other", object, Some.class, 12)}
     *
     * @param path   target path, relative to rest root (could contain String.format() placeholders: %s)
     * @param body   entity object (everything except {@link Entity} converted to JSON)
     * @param result result type (null or Void to ignore response body)
     * @param args   variables for path placeholders (String.format() arguments)
     * @param <R>    result type
     * @return mapped result object or null (if class not declared)
     */
    public <R> R post(final String path, final @Nullable Object body, final @Nullable Class<R> result,
                      final Object... args) {
        return handleShortcut(buildPost(path, body, args), result);
    }

    /**
     * POST call shortcut. Almost the same as jersey
     * {@code client.target(path).request().post(entity, new GenericType<List<Some>>(){}))}:
     * <p>
     * If body is already an {@link jakarta.ws.rs.client.Entity} - it will be used as is, other objects
     * would be converted to json entity ({@link Entity#json(Object)}).
     * <p>
     * For forms (urlencoded and multipart) use {@link #buildForm(String, Object...)}:
     * {@code buildForm("/path/%s/sub", 12).param("A", 1)..buildPost().invoke(Some.class)}.
     * <ul>
     *    <li>Exception thrown if the result is not successful (not 2xx)</li>
     *    <li>For {@link ru.vyarus.dropwizard.guice.test.jupiter.ext.rest.StubRest} resource exception
     *    propagated if no exception mappers registered</li>
     * </ul>
     * <p>
     * Additional headers, query params, etc. could be provided with defaults
     * (e.g. {@link #defaultHeader(String, Object)}). For additional request-specific configuration
     * use {@link #buildPost(String, Object, Object...)}.
     * <p>
     * String format could be used for path formatting:
     * {@code List<Some> res = client.post("/smth/%s/other", object, new GenericType<>(){}, 12)}
     *
     * @param path   target path, relative to rest root (could contain String.format() placeholders: %s)
     * @param body   entity object (everything except {@link Entity} converted to JSON)
     * @param result result type (null or Void to ignore response body)
     * @param args   variables for path placeholders (String.format() arguments)
     * @param <R>    result type
     * @return mapped result object or null (if class not declared)
     */
    public <R> R post(final String path, final @Nullable Object body, final @Nullable GenericType<R> result,
                      final Object... args) {
        return handleShortcut(buildPost(path, body, args), result);
    }

    /**
     * PUT call shortcut. Almost the same as jersey {@code client.target(path).request().put(entity, Void.class))}:
     * <p>
     * If body is already an {@link jakarta.ws.rs.client.Entity} - it will be used as is, other objects
     * would be converted to json entity ({@link Entity#json(Object)}).
     * <ul>
     *    <li>Exception thrown if the result is not successful (not 2xx)</li>
     *    <li>For {@link ru.vyarus.dropwizard.guice.test.jupiter.ext.rest.StubRest} resource exception
     *    propagated if no exception mappers registered</li>
     *    <li>Response body is ignored ({@link TestClientRequestBuilder#noBodyMappingForVoid()})</li>
     * </ul>
     * <p>
     * Additional headers, query params, etc. could be provided with defaults
     * (e.g. {@link #defaultHeader(String, Object)}). For additional request-specific configuration
     * use {@link #buildPut(String, Object, Object...)}.
     * <p>
     * String format could be used for path formatting:
     * {@code client.put("/smth/%s/other", object, 12)}
     *
     * @param path target path, relative to rest root (could contain String.format() placeholders: %s)
     * @param body entity object (everything except {@link Entity} converted to JSON)
     * @param args variables for path placeholders (String.format() arguments)
     */
    public void put(final String path, final Object body, final Object... args) {
        put(path, body, Void.class, args);
    }

    /**
     * PUT call shortcut. Almost the same as jersey {@code client.target(path).request().put(entity, Some.class))}:
     * <p>
     * If body is already an {@link jakarta.ws.rs.client.Entity} - it will be used as is, other objects
     * would be converted to json entity ({@link Entity#json(Object)}).
     * <ul>
     *    <li>Exception thrown if the result is not successful (not 2xx)</li>
     *    <li>For {@link ru.vyarus.dropwizard.guice.test.jupiter.ext.rest.StubRest} resource exception
     *    propagated if no exception mappers registered</li>
     * </ul>
     * <p>
     * Additional headers, query params, etc. could be provided with defaults
     * (e.g. {@link #defaultHeader(String, Object)}). For additional request-specific configuration
     * use {@link #buildPut(String, Object, Object...)}.
     * <p>
     * String format could be used for path formatting:
     * {@code Some res = client.put("/smth/%s/other", object, Some.class, 12)}
     *
     * @param path   target path, relative to rest root (could contain String.format() placeholders: %s)
     * @param body   entity object (everything except {@link Entity} converted to JSON)
     * @param result result type (null or Void to ignore response body)
     * @param args   variables for path placeholders (String.format() arguments)
     * @param <R>    result type
     * @return mapped result object or null (if class not declared)
     */
    public <R> R put(final String path, final Object body, final @Nullable Class<R> result, final Object... args) {
        return handleShortcut(buildPut(path, body, args), result);
    }

    /**
     * PUT call shortcut. Almost the same as jersey
     * {@code client.target(path).request().put(entity, new GenericType<List<Some>>(){}))}:
     * <p>
     * If body is already an {@link jakarta.ws.rs.client.Entity} - it will be used as is, other objects
     * would be converted to json entity ({@link Entity#json(Object)}).
     * <ul>
     *    <li>Exception thrown if the result is not successful (not 2xx)</li>
     *    <li>For {@link ru.vyarus.dropwizard.guice.test.jupiter.ext.rest.StubRest} resource exception
     *    propagated if no exception mappers registered</li>
     * </ul>
     * <p>
     * Additional headers, query params, etc. could be provided with defaults
     * (e.g. {@link #defaultHeader(String, Object)}). For additional request-specific configuration
     * use {@link #buildPut(String, Object, Object...)}.
     * <p>
     * String format could be used for path formatting:
     * {@code List<Some> res = client.put("/smth/%s/other", object, new GenericType<>(){}, 12)}
     *
     * @param path   target path, relative to rest root (could contain String.format() placeholders: %s)
     * @param body   entity object (everything except {@link Entity} converted to JSON)
     * @param result result type (null or Void to ignore response body)
     * @param args   variables for path placeholders (String.format() arguments)
     * @param <R>    result type
     * @return mapped result object or null (if class not declared)
     */
    public <R> R put(final String path, final Object body, final @Nullable GenericType<R> result,
                     final Object... args) {
        return handleShortcut(buildPut(path, body, args), result);
    }

    /**
     * PATCH call shortcut. Almost the same as jersey
     * {@code client.target(path).request().build("PATCH").invoke(Void.class))}:
     * <p>
     * If body is already an {@link jakarta.ws.rs.client.Entity} - it will be used as is, other objects
     * would be converted to json entity ({@link Entity#json(Object)}).
     * <p>
     * WARNING: in integration tests (real http call, not stub) the jersey client would use
     * {@link org.glassfish.jersey.client.HttpUrlConnectorProvider} which have problems with PATCH calls on JDK > 16
     * (requires additional --add-opens). To workaround this
     * {@link org.glassfish.jersey.apache5.connector.Apache5ConnectorProvider} could be used.
     * Guicey provides custom {@link ru.vyarus.dropwizard.guice.test.client.ApacheTestClientFactory} for using
     * apache client, which could be enabled with shortcut in test extensions (for example,
     * {@link ru.vyarus.dropwizard.guice.test.jupiter.TestDropwizardApp#useApacheClient()}).
     * <ul>
     *    <li>Exception thrown if the result is not successful (not 2xx)</li>
     *    <li>For {@link ru.vyarus.dropwizard.guice.test.jupiter.ext.rest.StubRest} resource exception
     *    propagated if no exception mappers registered</li>
     *    <li>Response body is ignored ({@link TestClientRequestBuilder#noBodyMappingForVoid()})</li>
     * </ul>
     * <p>
     * Additional headers, query params, etc. could be provided with defaults
     * (e.g. {@link #defaultHeader(String, Object)}). For additional request-specific configuration
     * use {@link #buildPatch(String, Object, Object...)}.
     * <p>
     * String format could be used for path formatting:
     * {@code client.patch("/smth/%s/other", object, 12)}
     *
     * @param path target path, relative to rest root (could contain String.format() placeholders: %s)
     * @param body entity object (everything except {@link Entity} converted to JSON)
     * @param args variables for path placeholders (String.format() arguments)
     */
    public void patch(final String path, final @Nullable Object body, final Object... args) {
        patch(path, body, Void.class, args);
    }

    /**
     * PATCH call shortcut. Almost the same as jersey
     * {@code client.target(path).request().build("PATCH").invoke(Some.class))}:
     * <p>
     * If body is already an {@link jakarta.ws.rs.client.Entity} - it will be used as is, other objects
     * would be converted to json entity ({@link Entity#json(Object)}).
     * <p>
     * WARNING: in integration tests (real http call, not stub) the jersey client would use
     * {@link org.glassfish.jersey.client.HttpUrlConnectorProvider} which have problems with PATCH calls on JDK > 16
     * (requires additional --add-opens). To workaround this
     * {@link org.glassfish.jersey.apache5.connector.Apache5ConnectorProvider} could be used.
     * Guicey provides custom {@link ru.vyarus.dropwizard.guice.test.client.ApacheTestClientFactory} for using
     * apache client, which could be enabled with shortcut in test extensions (for example,
     * {@link ru.vyarus.dropwizard.guice.test.jupiter.TestDropwizardApp#useApacheClient()}).
     * <ul>
     *    <li>Exception thrown if the result is not successful (not 2xx)</li>
     *    <li>For {@link ru.vyarus.dropwizard.guice.test.jupiter.ext.rest.StubRest} resource exception
     *    propagated if no exception mappers registered</li>
     * </ul>
     * <p>
     * Additional headers, query params, etc. could be provided with defaults
     * (e.g. {@link #defaultHeader(String, Object)}). For additional request-specific configuration
     * use {@link #buildPatch(String, Object, Object...)}.
     * <p>
     * String format could be used for path formatting:
     * {@code Some res = client.patch("/smth/%s/other", object, Some.class, 12)}
     *
     * @param path   target path, relative to rest root (could contain String.format() placeholders: %s)
     * @param body   entity object (everything except {@link Entity} converted to JSON)
     * @param result result type (null or Void to ignore response body)
     * @param args   variables for path placeholders (String.format() arguments)
     * @param <R>    result type
     * @return mapped result object or null (if class not declared)
     */
    public <R> R patch(final String path, final @Nullable Object body, final @Nullable Class<R> result,
                       final Object... args) {
        return handleShortcut(buildPatch(path, body, args), result);
    }

    /**
     * PATCH call shortcut. Almost the same as jersey
     * {@code client.target(path).request().build("PATCH").invoke(new GenericType<List<Some>>(){}))}:
     * <p>
     * If body is already an {@link jakarta.ws.rs.client.Entity} - it will be used as is, other objects
     * would be converted to json entity ({@link Entity#json(Object)}).
     * <p>
     * WARNING: in integration tests (real http call, not stub) the jersey client would use
     * {@link org.glassfish.jersey.client.HttpUrlConnectorProvider} which have problems with PATCH calls on JDK > 16
     * (requires additional --add-opens). To workaround this
     * {@link org.glassfish.jersey.apache5.connector.Apache5ConnectorProvider} could be used.
     * Guicey provides custom {@link ru.vyarus.dropwizard.guice.test.client.ApacheTestClientFactory} for using
     * apache client, which could be enabled with shortcut in test extensions (for example,
     * {@link ru.vyarus.dropwizard.guice.test.jupiter.TestDropwizardApp#useApacheClient()}).
     * <ul>
     *    <li>Exception thrown if the result is not successful (not 2xx)</li>
     *    <li>For {@link ru.vyarus.dropwizard.guice.test.jupiter.ext.rest.StubRest} resource exception
     *    propagated if no exception mappers registered</li>
     * </ul>
     * <p>
     * Additional headers, query params, etc. could be provided with defaults
     * (e.g. {@link #defaultHeader(String, Object)}). For additional request-specific configuration
     * use {@link #buildPatch(String, Object, Object...)}.
     * <p>
     * String format could be used for path formatting:
     * {@code List<Some> res = client.patch("/smth/%s/other", object, new GenericType<>(){}, 12)}
     *
     * @param path   target path, relative to rest root (could contain String.format() placeholders: %s)
     * @param body   entity object (everything except {@link Entity} converted to JSON)
     * @param result result type (null or Void to ignore response body)
     * @param args   variables for path placeholders (String.format() arguments)
     * @param <R>    result type
     * @return mapped result object or null (if class not declared)
     */
    public <R> R patch(final String path, final @Nullable Object body, final @Nullable GenericType<R> result,
                       final Object... args) {
        return handleShortcut(buildPatch(path, body, args), result);
    }

    /**
     * DELETE call shortcut. Almost the same as jersey {@code client.target(path).request().delete(Void.class)}:
     * <p>
     * According to spec, HTTP DELETE should not support body and so there are no shortcuts with body. If
     * you need to send DELETE with body use general builder:
     * {@code build(HttpMethod.DELETE, "/some/path", object).invoke(Some.class)}.
     * <ul>
     *    <li>Exception thrown if the result is not successful (not 2xx)</li>
     *    <li>For {@link ru.vyarus.dropwizard.guice.test.jupiter.ext.rest.StubRest} resource exception
     *    propagated if no exception mappers registered</li>
     *    <li>Response body is ignored ({@link TestClientRequestBuilder#noBodyMappingForVoid()})</li>
     * </ul>
     * <p>
     * Additional headers, query params, etc. could be provided with defaults
     * (e.g. {@link #defaultHeader(String, Object)}). For additional request-specific configuration
     * use {@link #buildDelete(String, Object...)}.
     * <p>
     * String format could be used for path formatting:
     * {@code client.delete("/smth/%s/other", 12)}
     *
     * @param path target path, relative to rest root  (could contain String.format() placeholders: %s)
     * @param args variables for path placeholders (String.format() arguments)
     */
    public void delete(final String path, final Object... args) {
        delete(path, Void.class, args);
    }

    /**
     * PUT call shortcut. Almost the same as jersey {@code client.target(path).request().put(entity, Some.class))}:
     * <p>
     * According to spec, HTTP DELETE should not support body and so there are no shortcuts with body. If
     * you need to send DELETE with body use general builder:
     * {@code build(HttpMethod.DELETE, "/some/path", object).invoke(Some.class)}.
     * <ul>
     *    <li>Exception thrown if the result is not successful (not 2xx)</li>
     *    <li>For {@link ru.vyarus.dropwizard.guice.test.jupiter.ext.rest.StubRest} resource exception
     *    propagated if no exception mappers registered</li>
     * </ul>
     * <p>
     * Additional headers, query params, etc. could be provided with defaults
     * (e.g. {@link #defaultHeader(String, Object)}). For additional request-specific configuration
     * use {@link #buildDelete(String, Object...)}.
     * <p>
     * String format could be used for path formatting:
     * {@code Some res = client.delete("/smth/%s/other", Some.class, 12)}
     *
     * @param path   target path, relative to rest root (could contain String.format() placeholders: %s)
     * @param result result type (null or Void to ignore response body)
     * @param args   variables for path placeholders (String.format() arguments)
     * @param <R>    result type
     * @return mapped result object or null (if class not declared)
     */
    public <R> R delete(final String path, final @Nullable Class<R> result, final Object... args) {
        return handleShortcut(buildDelete(path, args), result);
    }

    /**
     * DELETE call shortcut. Almost the same as jersey
     * {@code client.target(path).request().delete(entity, new GenericType<List<Some>>(){}))}:
     * <p>
     * According to spec, HTTP DELETE should not support body and so there are no shortcuts with body. If
     * you need to send DELETE with body use general builder:
     * {@code build(HttpMethod.DELETE, "/some/path", object).invoke(Some.class)}.
     * <ul>
     *    <li>Exception thrown if the result is not successful (not 2xx)</li>
     *    <li>For {@link ru.vyarus.dropwizard.guice.test.jupiter.ext.rest.StubRest} resource exception
     *    propagated if no exception mappers registered</li>
     * </ul>
     * <p>
     * Additional headers, query params, etc. could be provided with defaults
     * (e.g. {@link #defaultHeader(String, Object)}). For additional request-specific configuration
     * use {@link #buildDelete(String, Object...)}.
     * <p>
     * String format could be used for path formatting:
     * {@code Some res = client.delete("/smth/%s/other", new GenericType<>(){}, 12)}
     *
     * @param path   target path, relative to rest root (could contain String.format() placeholders: %s)
     * @param result result type (null or Void to ignore response body)
     * @param args   variables for path placeholders (String.format() arguments)
     * @param <R>    result type
     * @return mapped result object or null (if class not declared)
     */
    public <R> R delete(final String path, final @Nullable GenericType<R> result, final Object... args) {
        return handleShortcut(buildDelete(path, args), result);
    }

    // ------------------------------------------------------------------------ REQUEST BUILDERS

    /**
     * Generic request builder.
     * <p>
     * Body is not required. If body is an {@link jakarta.ws.rs.client.Entity} - it will be used as is, other objects
     * would be converted to json entity ({@link Entity#json(Object)}).
     * <p>
     * Defaults like {@link #defaultHeader(String, Object)} are applied.
     *
     * @param method http method
     * @param path   target path, relative to rest root (could contain String.format() placeholders: %s)
     * @param body   optional request body (everything except {@link Entity} converted to JSON)
     * @param args   variables for path placeholders (String.format() arguments)
     * @return request builder
     * @see jakarta.ws.rs.HttpMethod
     */
    public TestClientRequestBuilder build(final String method, final String path, final @Nullable Object body,
                                          final Object... args) {
        return new TestClientRequestBuilder(target(path, args), method,
                body != null ? getEntity(body) : null, defaults);
    }

    /**
     * GET request builder.
     * <p>
     * Example usage: {@code buildGet("/path/%s/sub", 12}.header("A", 1).invoke(Some.class)}
     * <p>
     * In simple cases use shortcut: {@link #get(String, Class, Object...)}.
     * <p>
     * For (urlencoded) forms use {@link #buildForm(String, Object...)}:
     * {@code buildForm("/path/%s/sub", 12).param("A", 1)..buildGet().invoke(Some.class)}.
     * <p>
     * Defaults like {@link #defaultHeader(String, Object)} are applied.
     *
     * @param path target path, relative to rest root (could contain String.format() placeholders: %s)
     * @param args variables for path placeholders (String.format() arguments)
     * @return request builder
     */
    public TestClientRequestBuilder buildGet(final String path, final Object... args) {
        return build(HttpMethod.GET, path, null, args);
    }

    /**
     * POST request builder.
     * <p>
     * Example usage: {@code buildPost("/path/%s/sub", object, 12}.header("A", 1).invoke(Some.class)}
     * <p>
     * In simple cases use shortcut: {@link #post(String, Object, Class, Object...)}.
     * <p>
     * For forms (urlencoded and multipart) use {@link #buildForm(String, Object...)}:
     * {@code buildForm("/path/%s/sub", 12).param("A", 1)..buildPost().invoke(Some.class)}.
     * <p>
     * Defaults like {@link #defaultHeader(String, Object)} are applied.
     *
     * @param path   target path, relative to rest root (could contain String.format() placeholders: %s)
     * @param entity request body (everything except {@link Entity} converted to JSON)
     * @param args   variables for path placeholders (String.format() arguments)
     * @return request builder
     */
    public TestClientRequestBuilder buildPost(final String path, final Object entity, final Object... args) {
        return build(HttpMethod.POST, path, entity, args);
    }

    /**
     * PUT request builder.
     * <p>
     * Example usage: {@code buildPut("/path/%s/sub", object, 12}.header("A", 1).invoke(Some.class)}
     * <p>
     * In simple cases use shortcut: {@link #put(String, Object, Class, Object...)}.
     * <p>
     * Defaults like {@link #defaultHeader(String, Object)} are applied.
     *
     * @param path   target path, relative to rest root (could contain String.format() placeholders: %s)
     * @param entity request body (everything except {@link Entity} converted to JSON)
     * @param args   variables for path placeholders (String.format() arguments)
     * @return request builder
     */
    public TestClientRequestBuilder buildPut(final String path, final Object entity, final Object... args) {
        return build(HttpMethod.PUT, path, entity, args);
    }

    /**
     * PATCH request builder.
     * <p>
     * Example usage: {@code buildPatch("/path/%s/sub", object, 12}.header("A", 1).invoke(Some.class)}
     * <p>
     * In simple cases use shortcut: {@link #patch(String, Object, Class, Object...)}.
     * <p>
     * Defaults like {@link #defaultHeader(String, Object)} are applied.
     * <p>
     * WARNING: in integration tests (real http call, not stub) the jersey client would use
     * {@link org.glassfish.jersey.client.HttpUrlConnectorProvider} which have problems with PATCH calls on JDK > 16
     * (requires additional --add-opens). To workaround this
     * {@link org.glassfish.jersey.apache5.connector.Apache5ConnectorProvider} could be used.
     * Guicey provides custom {@link ru.vyarus.dropwizard.guice.test.client.ApacheTestClientFactory} for using
     * apache client, which could be enabled with shortcut in test extensions (for example,
     * {@link ru.vyarus.dropwizard.guice.test.jupiter.TestDropwizardApp#useApacheClient()}).
     *
     * @param path   target path, relative to rest root (could contain String.format() placeholders: %s)
     * @param entity request body (everything except {@link Entity} converted to JSON)
     * @param args   variables for path placeholders (String.format() arguments)
     * @return request builder
     */
    public TestClientRequestBuilder buildPatch(final String path, final Object entity, final Object... args) {
        return build(HttpMethod.PATCH, path, entity, args);
    }

    /**
     * DELETE request builder.
     * <p>
     * Example usage: {@code buildDelete("/path/%s/sub", 12}.header("A", 1).invoke(Some.class)}
     * <p>
     * In simple cases use shortcut: {@link #delete(String, Class, Object...)}.
     * <p>
     * Defaults like {@link #defaultHeader(String, Object)} are applied.
     *
     * @param path target path, relative to rest root (could contain String.format() placeholders: %s)
     * @param args variables for path placeholders (String.format() arguments)
     * @return request builder
     */
    public TestClientRequestBuilder buildDelete(final String path, final Object... args) {
        return build(HttpMethod.DELETE, path, null, args);
    }

    /**
     * Build urlencoded or multipart form for GET (only urlencoded) and POST.
     * <p>
     * Important: multipart support requires an additional dependency:
     * 'org.glassfish.jersey.media:jersey-media-multipart'.
     * <p>
     * The type of the form is detected by provided values: if at least one value requires multipart, then
     * a multipart entity is created, otherwise urlencoded entity created. Only urlencoded entity could be used with
     * GET. Multipart form type could be explicitly forced with
     * {@link ru.vyarus.dropwizard.guice.test.client.builder.FormBuilder#forceMultipart()}.
     * <p>
     * Example urlencoded form usage:
     * <pre><code>
     *     buildForm(...)
     *     .param("name1", value1)
     *     .param("name2", value2)
     *     .buildPost()
     *     .invoke()
     * </code></pre>
     * <p>
     * Example multipart form usage:
     * <pre><code>
     *     buildForm(...)
     *     .param("name1", value1)
     *     .param("file", file)
     *     .buildPost()
     *     .invoke()
     * </code></pre>
     * <p>
     * Also, builder could be used just for entity building:
     * {@link ru.vyarus.dropwizard.guice.test.client.builder.FormBuilder#buildEntity()} (for manual entity usage).
     * <p>
     * Form build would automatically serialiaze java.util and java.time dates into String. To modify date
     * formats use {@link #defaultFormDateFormatter(java.text.DateFormat)} and
     * {@link #defaultFormDateTimeFormatter(java.time.format.DateTimeFormatter)} (or in builder directly:
     * {@link ru.vyarus.dropwizard.guice.test.client.builder.FormBuilder#formDateFormatter(java.text.DateFormat)},
     * {@link ru.vyarus.dropwizard.guice.test.client.builder.FormBuilder#formDateTimeFormatter(
     * java.time.format.DateTimeFormatter)}).
     * <p>
     * See {@link ru.vyarus.dropwizard.guice.test.client.builder.FormBuilder#param(String, Object)} for more details
     * about parameter values conversion.
     *
     * @param path target path, relative to rest root (could contain String.format() placeholders: %s)
     * @param args variables for path placeholders (String.format() arguments)
     * @return form builder
     */
    public FormBuilder buildForm(final String path, final Object... args) {
        return new FormBuilder(target(path, args), defaults);
    }

    @Override
    public String toString() {
        return "Client for: " + getRoot().getUri().toString();
    }

    /**
     * Check if provided value is already an entity.
     *
     * @param entity value to check
     * @return entity itself if value is an entity or json entity
     */
    protected Entity<?> getEntity(final Object entity) {
        final Entity<?> body;
        if (entity == null || entity instanceof Entity) {
            body = (Entity<?>) entity;
        } else {
            body = Entity.json(entity);
        }
        return body;
    }

    /**
     * Subclasses could override this method: this is required because it is not always possible to provide
     * the correct root target in the constructor (when this method is overridden root, provided in the constructor is
     * null).
     *
     * @return root target
     */
    protected WebTarget getRoot() {
        // for direct usage
        if (root != null) {
            return root.get();
        }
        // for extended classes which pass null as supplier
        throw new UnsupportedOperationException("Implementing class must override this method it it can't provider "
                + "a correct supplier in constructor");
    }

    private <R> R handleShortcut(final TestClientRequestBuilder request, final @Nullable Class<R> result) {
        return handleShortcut(request, result == null ? null : new GenericType<>(result));
    }

    private <R> R handleShortcut(final TestClientRequestBuilder request, final @Nullable GenericType<R> result) {
        // immediate result mapping with bypassing exceptions in rest stubs mode (if not exception mapper registered)
        return request.as(result);
    }

    /**
     * Create a client instance. It is assumed that all underlying classes would override this method to produce
     * sub clients of the same type.
     *
     * @param target target path
     * @return client insatnce
     */
    @SuppressWarnings("unchecked")
    protected T createClient(final String target) {
        return (T) new TestClient<>(() -> target(target), defaults);
    }
}
