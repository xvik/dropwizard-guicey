package ru.vyarus.dropwizard.guice.test.rest;

import javax.annotation.Nullable;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.Response;
import org.eclipse.jetty.http.HttpStatus;
import org.glassfish.jersey.test.JerseyTest;
import ru.vyarus.dropwizard.guice.module.installer.util.PathUtils;
import ru.vyarus.dropwizard.guice.test.rest.support.GuiceyJerseyTest;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

/**
 * REST client for test stubbed rest ({@link ru.vyarus.dropwizard.guice.test.jupiter.ext.rest.StubRest}).
 * <p>
 * {@link #client()} provides a raw client, configured with:
 * - Random port
 * - Requests logging ({@code @StubRest(logRequests = true)}, disabled by default)
 * - Enabled restricted headers and method workaround (for url connection, used by in-memory test container)
 * - Set default timeouts to avoid infinite calls
 * - Enabled multipart support (if available in classpath)
 * <p>
 * {@link #target(String...)} - shortcut for calling rest services with a relative urls (no server, port or rest
 * prefix). Without default* configuration (see below).
 * <p>
 * Shortcut rest call methods with response mapping:
 * - {@link #get(String, Class)}
 * - {@link #post(String, Object, Class)}
 * - {@link #put(String, Object, Class)}
 * - {@link #delete(String, Class)}
 * Putting null instead of result class implies void response. In this case, response status checked to be 200 or 204
 * (could be changed with {@link #defaultOk(Integer...)}).
 * <p>
 * To verify response headers use generic request method ({@link #request(String...)}):
 * for example, to request get response: {@code Response res = rest.request("/path/").get()}.
 * <p>
 * To simplify default shortcut methods usage, additional parameters like custom headers and query parameter are
 * configured as defaults:
 * - {@link #defaultHeader(String, String)}
 * - {@link #defaultQueryParam(String, String)}
 * - {@link #defaultAccept(String...)}
 * - {@link #defaultOk(Integer...)} used only to verify correct OK codes for void responses (for methods returning
 * mapped a mapped result: {@code get("/path/", null)}) - fail if the response status was not specified in defaultOk
 * (200, 204 by default). For methods returning a real result, status is not checked (result presence already means
 * correct execution)
 * <p>
 * By default, defaults are reset after each test. So defaults could be specified in test setup method (to apply the
 * same for all tests in class) or just before method call (in method test directly). Automatic rest could be disabled
 * with {@code @StubRest(autoReset = false)}.
 *
 * @author Vyacheslav Rusakov
 * @since 20.02.2025
 */
public class RestClient {

    private static final List<Integer> DEFAULT_OK = Arrays.asList(HttpStatus.OK_200, HttpStatus.NO_CONTENT_204);
    private final GuiceyJerseyTest jerseyTest;

    private final Map<String, String> defaultHeaders = new HashMap<>();
    private final Map<String, String> defaultQueryParams = new HashMap<>();
    private final List<String> defaultAccepts = new ArrayList<>();
    private List<Integer> defaultStatus = DEFAULT_OK;

    /**
     * Create client.
     *
     * @param jerseyTest jersey test instance
     */
    public RestClient(final GuiceyJerseyTest jerseyTest) {
        this.jerseyTest = jerseyTest;
    }

    /**
     * Creates a web target to be sent to the resource under testing.
     * When multiple parameters provided, they are connected with "/", avoiding duplicate slash appearances
     * so, for example, "app, path", "app/, /path" or any other variation would always lead to correct "app/path").
     * Essentially this is the same as using {@link WebTarget#path(String)} multiple times (after initial target
     * creation).
     * <p>
     * Example: {@code .target("/smth/").request().buildGet().invoke()}
     * <p>
     * WARNING: any specified defaults do not affect this method!
     * <p>
     * This is a generic method. Provided shortcuts (like {@link #get(String, Class)} should simplify usage.
     *
     * @param paths one or more path parts (assumed to be joined with '/') -
     *              overall, relative path (from tested application base URI) this web target should point to
     * @return jersey web target object
     */
    public WebTarget target(final String... paths) {
        return paths.length > 0 ? getJerseyTest().target(PathUtils.path(paths)) : getJerseyTest().target();
    }

    /**
     * Returns the pre-configured {@link javax.ws.rs.client.Client} for this test. For sending
     * requests prefer {@link #target(String...)}. Use {@link #target(String...)} method to avoid specifying
     * full target path.
     *
     * @return the {@link JerseyTest} configured {@link javax.ws.rs.client.Client}
     */
    public Client client() {
        return getJerseyTest().client();
    }

    /**
     * Just in case, it is not required when using any rest call method.
     *
     * @return root rest url
     */
    public URI getBaseUri() {
        return getJerseyTest().getRootUri();
    }

    /**
     * Apply a default header for shortcut rest call methods. Does not apply for {@link #target(String...)} method.
     *
     * @param name  header name
     * @param value header value
     * @return client itself for chained calls
     */
    public RestClient defaultHeader(final String name, final String value) {
        defaultHeaders.put(name, value);
        return this;
    }

    /**
     * Apply a default query param for shortcut rest call methods. Does not apply for {@link #target(String...)} method.
     *
     * @param name  parameter name
     * @param value parameter value
     * @return client itself for chained calls
     */
    public RestClient defaultQueryParam(final String name, final String value) {
        defaultQueryParams.put(name, value);
        return this;
    }

    /**
     * Set allowed response codes for void calls (by default, 200 and 204). For example, {@code get("/path/", null)}
     * would fail if response differs from specified. Note that status is not checked for responses, returning
     * result (result presence already indicates correct execution).
     * <p>
     * Does not apply for {@link #target(String...)} method.
     * <p>
     * Override previous setting.
     *
     * @param codes response codes allowed for void rest calls
     * @return client itself for chained calls
     */
    public RestClient defaultOk(final Integer... codes) {
        this.defaultStatus = Arrays.asList(codes);
        return this;
    }

    /**
     * Could be used for verifications in tests to avoid defaults collide.
     *
     * @return true if default headers specified
     */
    public boolean hasDefaultHeaders() {
        return !defaultHeaders.isEmpty();
    }

    /**
     * Could be used for verifications in tests to avoid defaults collide.
     *
     * @return true if default query params specified
     */
    public boolean hasDefaultQueryParams() {
        return !defaultQueryParams.isEmpty();
    }

    /**
     * Could be used for verifications in tests to avoid defaults collide.
     *
     * @return true if default accepts specified
     */
    public boolean hasDefaultAccepts() {
        return !defaultAccepts.isEmpty();
    }

    /**
     * Could be used for verifications in tests to avoid defaults collide.
     *
     * @return true if default void statuses changed
     */
    public boolean isDefaultStatusChanged() {
        return !defaultStatus.equals(DEFAULT_OK);
    }

    /**
     * Apply default Accept header for shortcut rest call methods. Does not apply for {@link #target(String...)} method.
     *
     * @param accept accept values
     * @return client itself for chained calls
     * @see javax.ws.rs.core.MediaType
     */
    public RestClient defaultAccept(final String... accept) {
        Collections.addAll(this.defaultAccepts, accept);
        return this;
    }

    /**
     * Create request for provided target with all defaults applied. Use to get the complete response object to validate
     * response headers: {@code Response res = request("/path/").get()}.
     *
     * @param paths target path, relative to rest root
     * @return request object, ready to be sent
     */
    public Invocation.Builder request(final String... paths) {
        return applyDefaults(applyDefaults(target(paths)).request());
    }

    /**
     * Simple GET call shortcut. Provided path should include only the target rest path.
     * <p>
     * To provide additional headers and query params see {@link #defaultHeader(String, String)}
     * ({@link #defaultAccept(String...)}) and {@link #defaultQueryParam(String, String)}.
     * For void responses (result class null) checks response status correctness (see {@link #defaultOk(Integer...)}).
     * <p>
     * For response headers validation, use raw {@code Response res = request("/path/").get()}.
     *
     * @param path   target path, relative to rest root
     * @param result result type (when null, accepts any 200 or 204 responses)
     * @param <T>    result type
     * @return mapped result object or null (if class not declared)
     */
    public <T> T get(final String path, final @Nullable Class<T> result) {
        if (result != null) {
            return request(path).get(result);
        } else {
            checkVoidResponse(() -> request(path).get());
            return null;
        }
    }

    /**
     * Simple POST call shortcut. Provided path should include only the target rest path.
     * Body object assumed to be a json entity (would be serialized as json). For file sending use method with generic
     * entity {@link #post(String, javax.ws.rs.client.Entity, Class)}.
     * <p>
     * To provide additional headers and query params see {@link #defaultHeader(String, String)}
     * ({@link #defaultAccept(String...)}) and {@link #defaultQueryParam(String, String)}.
     * For void responses (result class null) checks response status correctness (see {@link #defaultOk(Integer...)}).
     * <p>
     * For response headers validation, use raw {@code Response res = request("/path/").post(Entity.json(body)}.
     *
     * @param path   target path, relative to rest root
     * @param body   post body object (serialized as json)
     * @param result result type (when null, accepts any 200 or 204 responses)
     * @param <T>    result type
     * @return mapped result object or null (if class not declared)
     */
    public <T> T post(final String path, final @Nullable Object body, final @Nullable Class<T> result) {
        return post(path, Entity.json(body), result);
    }

    /**
     * Same as {@link #post(String, Object, Class)}, but accepts generic entity. Useful for sending multipart
     * requests.
     *
     * @param rootPath target path, relative to rest root
     * @param body     entity object
     * @param result   result type (when null, accepts any 200 or 204 responses)
     * @param <T>      result type
     * @return mapped result object or null (if class not declared)
     */
    public <T> T post(final String rootPath, final @Nullable Entity<?> body, final @Nullable Class<T> result) {
        if (result != null) {
            return request(rootPath).post(body, result);
        } else {
            checkVoidResponse(() -> request(rootPath).post(body));
            return null;
        }
    }

    /**
     * Simple PUT call shortcut. Provided path should include only the target rest path.
     * Body object assumed to be a json entity (would be serialized as json). For file sending use method with generic
     * entity {@link #put(String, javax.ws.rs.client.Entity, Class)}.
     * <p>
     * To provide additional headers and query params see {@link #defaultHeader(String, String)}
     * ({@link #defaultAccept(String...)}) and {@link #defaultQueryParam(String, String)}.
     * For void responses (result class null) checks response status correctness (see {@link #defaultOk(Integer...)}).
     * <p>
     * For response headers validation, use raw {@code Response res = request("/path/").put(Entity.json(body))}.
     *
     * @param path   target path, relative to rest root
     * @param body   put body object (serialized as json)
     * @param result result type (when null, accepts any 200 or 204 responses)
     * @param <T>    result type
     * @return mapped result object or null (if class not declared)
     */
    public <T> T put(final String path, final @Nullable Object body, final @Nullable Class<T> result) {
        return put(path, Entity.json(body), result);
    }

    /**
     * Same as {@link #put(String, Object, Class)}, but accepts generic entity. Useful for sending multipart
     * requests.
     *
     * @param path   target path, relative to rest root
     * @param body   post body object (serialized as json)
     * @param result result type (when null, accepts any 200 or 204 responses)
     * @param <T>    result type
     * @return mapped result object or null (if class not declared)
     */
    public <T> T put(final String path, final Entity<?> body, final @Nullable Class<T> result) {
        if (result != null) {
            return request(path).put(body, result);
        } else {
            checkVoidResponse(() -> request(path).put(body));
            return null;
        }
    }

    /**
     * Simple DELETE call shortcut. Provided path should include only the target rest path.
     * <p>
     * To provide additional headers and query params see {@link #defaultHeader(String, String)}
     * ({@link #defaultAccept(String...)}) and {@link #defaultQueryParam(String, String)}.
     * For void responses (result class null) checks response status correctness (see {@link #defaultOk(Integer...)}).
     * <p>
     * For response headers validation, use raw {@code Response res = request("/path/").delete()}.
     *
     * @param path   target path, relative to rest root
     * @param result result type (when null, accepts any 200 or 204 responses)
     * @param <T>    result type
     * @return mapped result object or null (if class not declared)
     */
    public <T> T delete(final String path, final @Nullable Class<T> result) {
        if (result != null) {
            return request(path).delete(result);
        } else {
            checkVoidResponse(() -> request(path).delete());
            return null;
        }
    }

    /**
     * Reset configured defaults.
     *
     * @return rest client itself for chained calls
     */
    public RestClient reset() {
        defaultHeaders.clear();
        defaultQueryParams.clear();
        defaultAccepts.clear();
        defaultStatus = DEFAULT_OK;
        return this;
    }

    private WebTarget applyDefaults(final WebTarget request) {
        WebTarget res = request;
        if (!defaultQueryParams.isEmpty()) {
            for (Map.Entry<String, String> entry : defaultQueryParams.entrySet()) {
                res = res.queryParam(entry.getKey(), entry.getValue());
            }
        }
        return res;
    }


    private Invocation.Builder applyDefaults(final Invocation.Builder request) {
        if (!defaultHeaders.isEmpty()) {
            request.headers(new MultivaluedHashMap<>(defaultHeaders));
        }
        if (!defaultAccepts.isEmpty()) {
            request.accept(defaultAccepts.toArray(new String[0]));
        }
        return request;
    }

    /**
     * Validates response to be 200 or 204 (no content). If not, throw exception with response body.
     *
     * @param call supplier providing response
     */
    private void checkVoidResponse(final Supplier<Response> call) {
        try (Response res = call.get()) {
            if (!defaultStatus.contains(res.getStatus())) {
                throw new IllegalStateException("Invalid response: " + res.getStatus() + "\n"
                        + res.readEntity(String.class));
            }
        }
    }

    private GuiceyJerseyTest getJerseyTest() {
        return requireNonNull(jerseyTest);
    }
}
