package ru.vyarus.dropwizard.guice.test.client.builder;

import com.google.common.base.Preconditions;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.CacheControl;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.RuntimeDelegate;
import org.eclipse.jetty.http.HttpHeader;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.message.internal.TracingLogger;
import org.jspecify.annotations.Nullable;
import ru.vyarus.dropwizard.guice.test.client.builder.track.RequestTracker;
import ru.vyarus.dropwizard.guice.test.client.builder.util.JerseyExceptionHandling;
import ru.vyarus.dropwizard.guice.test.client.builder.util.VoidBodyReader;

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Request builder. Provides all the same configuration methods as jersey client builder.
 * Supports default values, provided from the client.
 * <p>
 * Request could be executed:
 * <ul>
 *     <li>With direct response mapping: {@link #as(Class)}</li>
 *     <li>As void {@link #asVoid()} when response body is not important, just the success status</li>
 *     <li>With success validation {@link #expectSuccess(Integer...)} (it will also throw exception according to
 *     response status)</li>
 *     <li>With fail validation: {@link #expectFailure(Integer...)}</li>
 *     <li>With redirect validation: {@link #expectRedirect(Integer...)}</li>
 *     <li>Without status checks: {@link #invoke()}</li>
 * </ul>
 * <p>
 * In cases, when direct result mapping is not requested, a special response wrapper would be returned, supporting
 * assertions in a builder manner.
 * <p>
 * Builder does not hide jerse {@link jakarta.ws.rs.client.WebTarget} and
 * {@link jakarta.ws.rs.client.Invocation.Builder} objects: they could be configured manually with:
 * {@link #configurePath(Function)} and {@link #configureRequest(Consumer)}.
 *
 * @author Vyacheslav Rusakov
 * @since 12.09.2025
 */
@SuppressWarnings("PMD.TooManyMethods")
public class TestClientRequestBuilder {

    private final WebTarget target;
    private final String httpMethod;
    private final Entity<?> body;
    private final TestRequestConfig config;

    /**
     * Construct new request builder.
     *
     * @param target     request target
     * @param httpMethod request method
     * @param body       request entity (body)
     * @param defaults   default request configuration (common headers, cookies, path params, etc)
     */
    public TestClientRequestBuilder(final WebTarget target, final String httpMethod, final Entity<?> body,
                                    final @Nullable TestRequestConfig defaults) {
        this.target = target;
        this.httpMethod = Preconditions.checkNotNull(httpMethod, "Http method required");
        this.body = body;
        this.config = new TestRequestConfig(defaults);
    }

    // --------------------------------------------------------------------------------- RESPONSE CONFIG

    /**
     * Manual jersey target object configuration.
     *
     * @param modifier function, applying target configuration
     * @return builder instance for chained calls
     */
    public TestClientRequestBuilder configurePath(final Function<WebTarget, WebTarget> modifier) {
        this.config.configurePath(modifier);
        return this;
    }

    /**
     * Manual jersey request object configuration.
     *
     * @param modifier consumer, applying request configuration
     * @return builder instance for chained calls
     */
    public TestClientRequestBuilder configureRequest(final Consumer<Invocation.Builder> modifier) {
        this.config.configureRequest(modifier);
        return this;
    }

    /**
     * Disable redirects following to validate redirection correctness (or redirection fact).
     * <p>
     * Option is enabled automatically when {@link #expectRedirect(Integer...)} is used.
     *
     * @return builder instance for chained calls
     */
    public TestClientRequestBuilder notFollowRedirects() {
        this.config.property(ClientProperties.FOLLOW_REDIRECTS, Boolean.FALSE);
        return this;
    }

    /**
     * In theory, jersey should avoid body mapping when void response requested
     * ({@code builder.readEntity(Void.class)}), but jersey tries to map it and fils.
     * This shortcut registers a special body mapper which would completely ignore the response body.
     * <p>
     * Enabled automatically when {@link #asVoid()} is used.
     *
     * @return builder instance for chained calls
     */
    public TestClientRequestBuilder noBodyMappingForVoid() {
        this.config.register(VoidBodyReader.class);
        return this;
    }

    /**
     * Configure request Accept header (
     * {@link jakarta.ws.rs.client.Invocation.Builder#accept(jakarta.ws.rs.core.MediaType...)}).
     * <p>
     * Multiple calls override previous value.
     *
     * @param accept media types required for response
     * @return builder instance for chained calls
     * @see jakarta.ws.rs.core.MediaType
     */
    public TestClientRequestBuilder accept(final MediaType... accept) {
        final String[] res = Arrays.stream(accept).map(mediaType ->
                        RuntimeDelegate.getInstance().createHeaderDelegate(MediaType.class).toString(mediaType))
                .toArray(String[]::new);
        return accept(res);
    }

    /**
     * Configure request Accept header ({@link jakarta.ws.rs.client.Invocation.Builder#accept(String...)}).
     * <p>
     * Multiple calls override previous value.
     *
     * @param accept media types required for response
     * @return builder instance for chained calls
     * @see jakarta.ws.rs.core.MediaType
     */
    public TestClientRequestBuilder accept(final String... accept) {
        this.config.accept(accept);
        return this;
    }

    /**
     * Configure request Accept-Language header
     * ({@link jakarta.ws.rs.client.Invocation.Builder#acceptLanguage(java.util.Locale...)}).
     * <p>
     * Multiple calls override previous value.
     *
     * @param language languages to accept
     * @return builder instance for chained calls
     */
    public TestClientRequestBuilder acceptLanguage(final Locale... language) {
        final String[] res = Arrays.stream(language).map(Locale::toString).toArray(String[]::new);
        this.config.acceptLanguage(res);
        return this;
    }

    /**
     * Configure request Accept-Language header
     * ({@link jakarta.ws.rs.client.Invocation.Builder#acceptLanguage(String...)}).
     * <p>
     * Multiple calls override previous value.
     *
     * @param language languages to accept
     * @return builder instance for chained calls
     */
    public TestClientRequestBuilder acceptLanguage(final String... language) {
        this.config.acceptLanguage(language);
        return this;
    }

    /**
     * Configure request Accept-Encoding header
     * ({@link jakarta.ws.rs.client.Invocation.Builder#acceptEncoding(String...)}).
     * <p>
     * Multiple calls override previous value.
     *
     * @param encodings encodings to accept
     * @return builder instance for chained calls
     */
    public TestClientRequestBuilder acceptEncoding(final String... encodings) {
        this.config.acceptEncoding(encodings);
        return this;
    }

    /**
     * Configure request query parameter ({@link jakarta.ws.rs.client.WebTarget#queryParam(String, Object...)}).
     * <p>
     * Note: jersey api supports multiple query parameters with the same name (in this case multiple parameters
     * added). The same result could be achieved by passing list or array into this api.
     * <p>
     * Multiple calls with the same name override the previous value (the only way to specify multiple values is
     * using list or array as value).
     *
     * @param name  query parameter name
     * @param value parameter value (list or array for multiple values)
     * @return builder instance for chained calls
     */
    public TestClientRequestBuilder queryParam(final String name, final Object value) {
        this.config.queryParam(name, value);
        return this;
    }

    /**
     * Configure multiple query params at once.
     * <p>
     * Note: jersey api supports multiple query parameters with the same name (in this case multiple parameters
     * added). The same result could be achieved by passing list or array into this api.
     * <p>
     * Multiple calls append to previous configurations (parameters with the same name are overridden)
     *
     * @param params query params map
     * @return builder instance for chained calls
     */
    public TestClientRequestBuilder queryParams(final Map<String, ?> params) {
        for (Map.Entry<String, ?> entry : params.entrySet()) {
            this.config.queryParam(entry.getKey(), entry.getValue());
        }
        return this;
    }

    /**
     * Configure request matrix parameter ({@link jakarta.ws.rs.client.WebTarget#matrixParam(String, Object...)}).
     * <p>
     * Note: jersey api supports multiple matrix parameters with the same name (in this case multiple parameters
     * added). The same result could be achieved by passing list or array into this api.
     * <p>
     * Multiple calls with the same name override the previous value (the only way to specify multiple values is
     * using list or array as value).
     *
     * @param name  matrix parameter name
     * @param value parameter value (list or array for multiple values)
     * @return builder instance for chained calls
     */
    public TestClientRequestBuilder matrixParam(final String name, final Object value) {
        this.config.matrixParam(name, value);
        return this;
    }

    /**
     * Configure multiple matrix params at once.
     * <p>
     * Note: jersey api supports multiple matrix parameters with the same name (in this case multiple parameters
     * added). The same result could be achieved by passing list or array into this api.
     * <p>
     * Multiple calls append to previous configurations (parameters with the same name are overridden)
     *
     * @param params query params map
     * @return builder instance for chained calls
     */
    public TestClientRequestBuilder matrixParams(final Map<String, ?> params) {
        for (Map.Entry<String, ?> entry : params.entrySet()) {
            this.config.matrixParam(entry.getKey(), entry.getValue());
        }
        return this;
    }

    /**
     * Configure request path parameter
     * ({@link jakarta.ws.rs.client.WebTarget#resolveTemplateFromEncoded(String, Object)}).
     * <p>
     * Note: parameter value assumed to be encoded to be able to specify matrix parameters (in the middle of the path):
     * {@code pathParam("var", "/path;var=1;val=2")}.
     *
     * @param name  parameter name
     * @param value parameter value
     * @return builder instance for chained calls
     */
    public TestClientRequestBuilder pathParam(final String name, final Object value) {
        this.config.pathParam(name, value);
        return this;
    }

    /**
     * Configure multiple path params at once.
     * <p>
     * Multiple calls append to previous configurations (parameters with the same name are overridden)
     *
     * @param params parameters map
     * @return builder instance for chained calls
     */
    public TestClientRequestBuilder pathParams(final Map<String, ?> params) {
        for (Map.Entry<String, ?> entry : params.entrySet()) {
            this.config.pathParam(entry.getKey(), entry.getValue());
        }
        return this;
    }

    /**
     * Configure request header ({@link jakarta.ws.rs.client.Invocation.Builder#header(String, Object)}).
     * <p>
     * Multiple calls override previous value.
     *
     * @param name  header name
     * @param value header value
     * @return builder instance for chained calls
     */
    public TestClientRequestBuilder header(final HttpHeader name, final String value) {
        return header(name.toString(), value);
    }

    /**
     * Configure request header ({@link jakarta.ws.rs.client.Invocation.Builder#header(String, Object)}).
     * <p>
     * Multiple calls override previous value.
     *
     * @param name  header name
     * @param value header value
     * @return builder instance for chained calls
     */
    public TestClientRequestBuilder header(final String name, final String value) {
        this.config.header(name, value);
        return this;
    }

    /**
     * Configure multiple headers at once.
     * <p>
     * Multiple calls append to previous configurations (headers with the same name are overridden)
     *
     * @param headers headers map
     * @return builder instance for chained calls
     */
    public TestClientRequestBuilder headers(final Map<String, ?> headers) {
        for (Map.Entry<String, ?> entry : headers.entrySet()) {
            this.config.header(entry.getKey(), entry.getValue());
        }
        return this;
    }

    /**
     * Configure request cookie ({@link jakarta.ws.rs.client.Invocation.Builder#cookie(String, String)}.
     * <p>
     * Multiple calls override previous value.
     *
     * @param name  cookie name
     * @param value cookie value
     * @return builder instance for chained calls
     */
    public TestClientRequestBuilder cookie(final String name, final String value) {
        return cookie(new NewCookie.Builder(name)
                .value(value)
                .build());
    }

    /**
     * Configure request cookie ({@link jakarta.ws.rs.client.Invocation.Builder#cookie(jakarta.ws.rs.core.Cookie)}.
     * <p>
     * Use cookie builder: {@code new NewCookie.Builder(name).value(value).build()}.
     * <p>
     * Multiple calls override previous value.
     *
     * @param cookie cookie value
     * @return builder instance for chained calls
     * @see jakarta.ws.rs.core.NewCookie
     */
    public TestClientRequestBuilder cookie(final Cookie cookie) {
        this.config.cookie(cookie);
        return this;
    }

    /**
     * Configure multiple cookies at once.
     * <p>
     * Multiple calls append to previous configurations (cookies with the same name are overridden).
     *
     * @param cookies cookie map
     * @return builder instance for chained calls
     */
    public TestClientRequestBuilder cookies(final Map<String, String> cookies) {
        for (Map.Entry<String, String> entry : cookies.entrySet()) {
            cookie(entry.getKey(), entry.getValue());
        }
        return this;
    }

    /**
     * Jersey client property configuration ({@link org.glassfish.jersey.client.JerseyClient#property(String, Object)}).
     * <p>
     * Multiple calls override previous value.
     *
     * @param name  property name
     * @param value property value
     * @return builder instance for chained calls
     * @see org.glassfish.jersey.client.ClientProperties
     */
    public TestClientRequestBuilder property(final String name, final Object value) {
        this.config.property(name, value);
        return this;
    }

    /**
     * Configure multiple properties at once.
     * <p>
     * Multiple calls append to previous configurations (properties with the same name are overridden).
     *
     * @param props properties map
     * @return builder instance for chained calls
     */
    public TestClientRequestBuilder properties(final Map<String, ?> props) {
        for (Map.Entry<String, ?> entry : props.entrySet()) {
            this.config.property(entry.getKey(), entry.getValue());
        }
        return this;
    }

    /**
     * Jersey client extension registration ({@link org.glassfish.jersey.client.JerseyClient#register(Class)}).
     * Could be useful, for example, to register custom {@link jakarta.ws.rs.ext.MessageBodyReader}.
     * <p>
     * Multiple registrations of the same class will be ignored (extensions tracked by type).
     *
     * @param type extension class
     * @return builder instance for chained calls
     */
    public TestClientRequestBuilder register(final Class<?> type) {
        this.config.register(type);
        return this;
    }

    /**
     * Jersey client extension registration ({@link org.glassfish.jersey.client.JerseyClient#register(Object)}).
     * Could be useful, for example, to register custom {@link jakarta.ws.rs.ext.MessageBodyReader} (as instance).
     * <p>
     * When called with different instances of the same class: only the latest registration will be used
     * (extensions tracked by type).
     *
     * @param extension extensions instance
     * @return builder instance for chained calls
     */
    public TestClientRequestBuilder register(final Object extension) {
        this.config.register(extension);
        return this;
    }

    /**
     * Configure request Cache-Control header
     * ({@link jakarta.ws.rs.client.Invocation.Builder#cacheControl(CacheControl)}).
     * <p>
     * Multiple calls override previous value.
     *
     * @param cacheControl cache control header value (string) to apply to request.
     * @return builder instance for chained calls
     */
    public TestClientRequestBuilder cacheControl(final String cacheControl) {
        this.config.cacheControl(cacheControl);
        return this;
    }

    /**
     * Configure request Cache-Control header
     * ({@link jakarta.ws.rs.client.Invocation.Builder#cacheControl(CacheControl)}).
     * <p>
     * Multiple calls override previous value.
     *
     * @param cacheControl cache control settings to apply to request.
     * @return builder instance for chained calls
     */
    public TestClientRequestBuilder cacheControl(final CacheControl cacheControl) {
        this.config.cacheControl(cacheControl);
        return this;
    }

    /**
     * Enable console reporting for configured default values and applied request customizations.
     *
     * @return builder instance for chained calls
     */
    public TestClientRequestBuilder debug() {
        this.config.debug(true);
        return this;
    }

    /**
     * Shortcut for jersey trace requesting: jersey should return trace as X-Jersey-Tracing-NNN headers.
     * <p>
     * WARNING: Tracing support MUST be <a
     * href="https://eclipse-ee4j.github.io/jersey.github.io/documentation/latest/monitoring_tracing.html#d0e17551">
     * enabled on server</a> with:
     * {@code environment.jersey().property(ServerProperties.TRACING, TracingConfig.ON_DEMAND.name());}
     * <p>
     * This is assumed to be used for remote apis.
     *
     * @return builder instance for chained calls
     */
    public TestClientRequestBuilder enableJerseyTrace() {
        return enableJerseyTrace(TracingLogger.Level.SUMMARY);
    }

    /**
     * {@link #enableJerseyTrace()} with custom trace level.
     *
     * @param level trace level
     * @return builder instance for chained calls
     */
    public TestClientRequestBuilder enableJerseyTrace(final TracingLogger.Level level) {
        header(TracingLogger.HEADER_ACCEPT, "true");
        header(TracingLogger.HEADER_THRESHOLD, level.name());
        return this;
    }

    /**
     * Assert {@link jakarta.ws.rs.client.WebTarget} and {@link jakarta.ws.rs.client.Invocation.Builder}
     * objects configurations. Could be used to verify request correctness.
     * <p>
     * Implicitly enables {@link #debug()}, so all applied configurations would be printed to console first and
     * then assertions applied.
     * <p>
     * Assertions executed just before request execution.
     *
     * @param action assertion action
     * @return builder instance for chanined calls
     */
    public TestClientRequestBuilder assertRequest(final Consumer<RequestTracker> action) {
        this.config.assertRequest(action);
        return this;
    }

    // --------------------------------------------------------------------------------- THE CALL

    /**
     * Invoke request and expect successful status (2xx). Response body is ignored.
     * <p>
     * In case of not successful request, exception would be thrown according to response status.
     * <p>
     * Note: this option would implicitly enable {@link #noBodyMappingForVoid()}.
     * <p>
     * Under {@link ru.vyarus.dropwizard.guice.test.jupiter.ext.rest.StubRest} it will bypass original exception
     * (when no exception mappers registered).
     */
    @SuppressWarnings("PMD.LinguisticNaming")
    public void asVoid() {
        as(Void.class);
    }

    /**
     * Invoke request and expect successful status (2xx). Response body is mapped to the specified type.
     * This is essentially the same as jersey shortcut methods like {@code response.get(SomeEntity.class)}.
     * <p>
     * In case of not successful request, exception would be thrown according to response status.
     * <p>
     * Under {@link ru.vyarus.dropwizard.guice.test.jupiter.ext.rest.StubRest} it will bypass original exception
     * (when no exception mappers registered).
     *
     * @param type response type
     * @param <T>  response type
     * @return mapped response instance
     */
    public <T> T as(final @Nullable Class<T> type) {
        return as(type == null ? new GenericType<>(Void.class) : new GenericType<>(type));
    }

    /**
     * Invoke request and expect successful status (2xx). Response body is mapped to the specified type.
     * This is essentially the same as jersey shortcut methods like
     * {@code response.get(new GenericType<SomeEntity>(){}}.
     * <p>
     * In case of not successful request, exception would be thrown according to response status.
     * <p>
     * Under {@link ru.vyarus.dropwizard.guice.test.jupiter.ext.rest.StubRest} it will bypass original exception
     * (when no exception mappers registered).
     *
     * @param type response type
     * @param <T>  response type
     * @return mapped response instance
     */
    public <T> T as(final @Nullable GenericType<T> type) {
        final GenericType<T> resType = type == null ? new GenericType<>(Void.class) : type;
        if (resType.getRawType().equals(Void.class)) {
            // avoid errors on void mapping
            noBodyMappingForVoid();
        }
        final Invocation.Builder request = this.config.applyRequestConfiguration(this.target);
        // important: api with the result type check for throwing error on not successful response
        if (body == null) {
            return request.method(httpMethod, resType);
        } else {
            return request.method(httpMethod, body, resType);
        }
    }

    /**
     * Shortcut to {@link #as(Class)}.
     *
     * @return response body as string
     */
    public String asString() {
        return as(String.class);
    }

    /**
     * Invoke request without status validation (same as jersey
     * {@link jakarta.ws.rs.client.Invocation.Builder#invoke()}).
     * For automatic status validation see {@link #expectSuccess(Integer...)}, {@link #expectFailure(Integer...)}
     * and {@link #expectRedirect(Integer...)}.
     * <p>
     * Under {@link ru.vyarus.dropwizard.guice.test.jupiter.ext.rest.StubRest} it will bypass original exception
     * (when no exception mappers registered).
     * <p>
     * Note: IDE may highlight that method must be used under 'try-with-resources` (because the underlying response
     * must be correctly closed), but you can ignore it, because all responses are closed automatically.
     *
     * @return response wrapper object with additional assertions
     */
    public TestClientResponse invoke() {
        final Invocation.Builder request = this.config.applyRequestConfiguration(this.target);
        final Response response;
        if (body == null) {
            response = request.method(httpMethod);
        } else {
            response = request.method(httpMethod, body);
        }
        return new TestClientResponse(response);
    }

    /**
     * Invoke a request with status validation. It will throw an exception for non 2xx response
     * (same as {@link #as(Class)}). Will throw assertion error if response status does not match.
     * <p>
     * Under {@link ru.vyarus.dropwizard.guice.test.jupiter.ext.rest.StubRest} it will bypass original exception
     * (when no exception mappers registered).
     * <p>
     * Note: IDE may highlight that method must be used under 'try-with-resources` (because the underlying response
     * must be correctly closed), but you can ignore it, because all responses are closed automatically.
     *
     * @param statuses (optional) statuses to match (when exact status validation is important)
     * @return response wrapper object with additional assertions
     */
    public TestClientResponse expectSuccess(final Integer... statuses) {
        final TestClientResponse response = invoke();
        // unify behavior with jersey auto conversion case (so jersey would throw the same exceptions
        // in case of not successful response)
        JerseyExceptionHandling.throwIfNotSuccess(response.asResponse());
        return statuses.length > 0 ? response.assertStatus(statuses) : response;
    }

    /**
     * Invoke a request with status validation. Will throw assertion error if response status is success or
     * does not match one of the provided statuses.
     * <p>
     * Suitable for testing error response (or when exception mapped by some exception mapper).
     * <p>
     * Under {@link ru.vyarus.dropwizard.guice.test.jupiter.ext.rest.StubRest} it will bypass original exception
     * (when no exception mappers registered).
     * <p>
     * Note: IDE may highlight that method must be used under 'try-with-resources` (because the underlying response
     * must be correctly closed), but you can ignore it, because all responses are closed automatically.
     *
     * @param statuses (optional) statuses to match (when exact status validation is important)
     * @return response wrapper object with additional assertions
     */
    public TestClientResponse expectFailure(final Integer... statuses) {
        final TestClientResponse response = invoke().assertFail();
        return statuses.length > 0 ? response.assertStatus(statuses) : response;
    }

    /**
     * Invoke a request with status validation. Will throw assertion error if response status is not redirect or
     * does not match one of provided statuses.
     * <p>
     * Note: implicitly enables {@link #notFollowRedirects()}.
     * <p>
     * Under {@link ru.vyarus.dropwizard.guice.test.jupiter.ext.rest.StubRest} it will bypass original exception
     * (when no exception mappers registered).
     * <p>
     * Note: IDE may highlight that method must be used under 'try-with-resources` (because the underlying response
     * must be correctly closed), but you can ignore it, because all responses are closed automatically.
     *
     * @param statuses (optional) statuses to match (when exact status validation is important)
     * @return response wrapper object with additional assertions
     */
    public TestClientResponse expectRedirect(final Integer... statuses) {
        // disable auto redirects
        notFollowRedirects();
        final TestClientResponse response = invoke().assertRedirect();
        return statuses.length > 0 ? response.assertStatus(statuses) : response;
    }

    /**
     * @return request configuration
     */
    public TestRequestConfig getConfig() {
        return config;
    }

    @Override
    public String toString() {
        return "Request builder: " + httpMethod + " " + target.getUri().toString();
    }
}
