package ru.vyarus.dropwizard.guice.test.client.builder;

import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.CacheControl;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.ext.RuntimeDelegate;
import org.eclipse.jetty.http.HttpHeader;
import ru.vyarus.dropwizard.guice.test.client.TestClient;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Defaults configuration for {@link ru.vyarus.dropwizard.guice.test.client.TestClient}. Extracted to make
 * client class more readable.
 * <p>
 * Defaults could be declared for client to be applied in all clients. Sub-clients, created from this client
 * will inherit these defaults.
 *
 * @param <T> actual client type
 * @author Vyacheslav Rusakov
 * @since 23.09.2025
 */
@SuppressWarnings({"PMD.TooManyMethods", "PMD.ExcessivePublicCount"})
public abstract class TestClientDefaults<T extends TestClient<?>> {

    /**
     * Default configuration for all requests (and sub clients).
     */
    protected final TestRequestConfig defaults;

    /**
     * Creates new defaults instance.
     *
     * @param defaults default configuration for all requests (and sub clients)
     */
    public TestClientDefaults(final TestRequestConfig defaults) {
        this.defaults = defaults;
    }

    /**
     * Configure default header for all requests
     * ({@link jakarta.ws.rs.client.Invocation.Builder#header(String, Object)}).
     * <p>
     * Multiple calls override previous value.
     *
     * @param name  header name
     * @param value header value
     * @return builder instance for chained calls
     */
    public T defaultHeader(final HttpHeader name, final Object value) {
        return defaultHeader(name.toString(), value);
    }

    /**
     * Configure default header for all requests
     * ({@link jakarta.ws.rs.client.Invocation.Builder#header(String, Object)}).
     * <p>
     * Multiple calls override previous value.
     *
     * @param name  header name
     * @param value header value
     * @return builder instance for chained calls
     */
    public T defaultHeader(final String name, final Object value) {
        return defaultHeader(name, () -> value);
    }

    /**
     * Configure default header for all requests
     * ({@link jakarta.ws.rs.client.Invocation.Builder#header(String, Object)}).
     * <p>
     * Multiple calls override previous value.
     *
     * @param name     header name
     * @param supplier header value supplier
     * @return builder instance for chained calls
     */
    public T defaultHeader(final HttpHeader name, final Supplier<Object> supplier) {
        return defaultHeader(name.toString(), supplier);
    }

    /**
     * Configure default header for all requests
     * ({@link jakarta.ws.rs.client.Invocation.Builder#header(String, Object)}).
     * <p>
     * Multiple calls override previous value.
     *
     * @param name     header name
     * @param supplier header value supplier
     * @return builder instance for chained calls
     */
    public T defaultHeader(final String name, final Supplier<Object> supplier) {
        defaults.header(name, supplier);
        return self();
    }

    /**
     * Configure default query parameter for all requests
     * ({@link jakarta.ws.rs.client.WebTarget#queryParam(String, Object...)}).
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
    public T defaultQueryParam(final String name, final Object value) {
        return defaultQueryParam(name, () -> value);
    }

    /**
     * Configure default query parameter for all requests
     * ({@link jakarta.ws.rs.client.WebTarget#queryParam(String, Object...)}).
     * <p>
     * Note: jersey api supports multiple query parameters with the same name (in this case multiple parameters
     * added). The same result could be achieved by passing list or array into this api.
     * <p>
     * Multiple calls with the same name override the previous value (the only way to specify multiple values is
     * using list or array as value).
     *
     * @param name     query parameter name
     * @param supplier value supplier (list or array for multiple values)
     * @return builder instance for chained calls
     */
    public T defaultQueryParam(final String name, final Supplier<Object> supplier) {
        defaults.queryParam(name, supplier);
        return self();
    }

    /**
     * Configure default matrix parameter for all requests
     * ({@link jakarta.ws.rs.client.WebTarget#matrixParam(String, Object...)}).
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
    public T defaultMatrixParam(final String name, final Object value) {
        return defaultMatrixParam(name, () -> value);
    }

    /**
     * Configure default matrix parameter for all requests
     * ({@link jakarta.ws.rs.client.WebTarget#matrixParam(String, Object...)}).
     * <p>
     * Note: jersey api supports multiple matrix parameters with the same name (in this case multiple parameters
     * added). The same result could be achieved by passing list or array into this api.
     * <p>
     * Multiple calls with the same name override the previous value (the only way to specify multiple values is
     * using list or array as value).
     *
     * @param name     matrix parameter name
     * @param supplier value supplier (list or array for multiple values)
     * @return builder instance for chained calls
     */
    public T defaultMatrixParam(final String name, final Supplier<Object> supplier) {
        defaults.matrixParam(name, supplier);
        return self();
    }

    /**
     * Configure default path parameter for all requests
     * ({@link jakarta.ws.rs.client.WebTarget#resolveTemplateFromEncoded(String, Object)}).
     * <p>
     * Note: parameter value assumed to be encoded to be able to specify matrix parameters (in the middle of the path):
     * {@code pathParam("var", "/path;var=1;val=2")}.
     *
     * @param name  parameter name
     * @param value parameter value
     * @return builder instance for chained calls
     */
    public T defaultPathParam(final String name, final Object value) {
        return defaultPathParam(name, () -> value);
    }

    /**
     * Configure default path parameter for all requests
     * ({@link jakarta.ws.rs.client.WebTarget#resolveTemplateFromEncoded(String, Object)}).
     *
     * @param name     parameter name
     * @param supplier parameter value supplier
     * @return builder instance for chained calls
     */
    public T defaultPathParam(final String name, final Supplier<Object> supplier) {
        defaults.pathParam(name, supplier);
        return self();
    }

    /**
     * Configure default jersey client property applied to all requests
     * ({@link org.glassfish.jersey.client.JerseyClient#property(String, Object)}).
     * <p>
     * Multiple calls override previous value.
     *
     * @param name  property name
     * @param value property value
     * @return builder instance for chained calls
     * @see org.glassfish.jersey.client.ClientProperties
     */
    public T defaultProperty(final String name, final Object value) {
        return defaultProperty(name, () -> value);
    }

    /**
     * Configure default jersey client property applied to all requests
     * ({@link org.glassfish.jersey.client.JerseyClient#property(String, Object)}).
     * <p>
     * Multiple calls override previous value.
     *
     * @param name     property name
     * @param supplier property value supplier
     * @return builder instance for chained calls
     * @see org.glassfish.jersey.client.ClientProperties
     */
    public T defaultProperty(final String name, final Supplier<Object> supplier) {
        defaults.property(name, supplier);
        return self();
    }

    /**
     * Configure default jersey client extension for all requests
     * ({@link org.glassfish.jersey.client.JerseyClient#register(Class)}).
     * Could be useful, for example, to register custom {@link jakarta.ws.rs.ext.MessageBodyReader}.
     * <p>
     * Multiple registrations of the same class will be ignored (extensions tracked by type).
     *
     * @param type extension class
     * @return builder instance for chained calls
     */
    public T defaultRegister(final Class<?> type) {
        defaults.register(type);
        return self();
    }

    /**
     * Configure default jersey client extension for all requests
     * ({@link org.glassfish.jersey.client.JerseyClient#register(Object)}).
     * Could be useful, for example, to register custom {@link jakarta.ws.rs.ext.MessageBodyReader} (as instance).
     * <p>
     * When called with different instances of the same class: only the latest registration will be used
     * (extensions tracked by type).
     *
     * @param extension extensions instance
     * @return builder instance for chained calls
     */
    public T defaultRegister(final Object extension) {
        defaults.register(extension);
        return self();
    }

    /**
     * Configure default jersey client extension for all requests
     * ({@link org.glassfish.jersey.client.JerseyClient#register(Object)}).
     * Supplier could return either class or extension instance.
     * <p>
     * When called with different instances of the same class: only the latest registration will be used
     * (extensions tracked by type).
     *
     * @param type     extension type
     * @param supplier extension instance or null (for class)
     * @param <K>      extension type
     * @return builder instance for chained calls
     */
    public <K> T defaultRegister(final Class<K> type, final Supplier<K> supplier) {
        defaults.register(type, supplier);
        return self();
    }

    /**
     * Configure default java.util date formatter for form fields (used in
     * {@link ru.vyarus.dropwizard.guice.test.client.TestClient#buildForm(String, Object...)}).
     *
     * @param formatter date formatter
     * @return builder instance for chained calls
     * @see #defaultFormDateFormat(String) for short declaration
     */
    public T defaultFormDateFormatter(final DateFormat formatter) {
        defaults.formDateFormatter(formatter);
        return self();
    }

    /**
     * Configure default java.time formatter for form fields (used in
     * {@link ru.vyarus.dropwizard.guice.test.client.TestClient#buildForm(String, Object...)}).
     *
     * @param formatter date formatter
     * @return builder instance for chained calls
     * @see #defaultFormDateFormat(String) for short declaration
     */
    public T defaultFormDateTimeFormatter(final DateTimeFormatter formatter) {
        defaults.formDateTimeFormatter(formatter);
        return self();
    }

    /**
     * Shortcut to configure both date formatters with the same pattern.
     *
     * @param format format
     * @return builder instance for chained calls
     * @see #defaultFormDateFormatter(java.text.DateFormat)
     * @see #defaultFormDateTimeFormatter(java.time.format.DateTimeFormatter)
     */
    @SuppressWarnings("PMD.SimpleDateFormatNeedsLocale")
    public T defaultFormDateFormat(final String format) {
        defaults.formDateFormatter(new SimpleDateFormat(format));
        defaults.formDateTimeFormatter(DateTimeFormatter.ofPattern(format));
        return self();
    }

    /**
     * Configure default cookie for all requests
     * ({@link jakarta.ws.rs.client.Invocation.Builder#cookie(jakarta.ws.rs.core.Cookie)}.
     * <p>
     * Multiple calls override previous value.
     *
     * @param name  cookie name
     * @param value cookie value
     * @return builder instance for chained calls
     * @see jakarta.ws.rs.core.NewCookie
     */
    public T defaultCookie(final String name, final String value) {
        return defaultCookie(name, () -> new NewCookie.Builder(name).value(value).build());
    }

    /**
     * Configure default cookie for all requests
     * ({@link jakarta.ws.rs.client.Invocation.Builder#cookie(jakarta.ws.rs.core.Cookie)}.
     * <p>
     * Use cookie builder: {@code new NewCookie.Builder(name).value(value).build()}.
     * <p>
     * Multiple calls override previous value.
     *
     * @param cookie cookie value
     * @return builder instance for chained calls
     * @see jakarta.ws.rs.core.NewCookie
     */
    public T defaultCookie(final Cookie cookie) {
        return defaultCookie(cookie.getName(), () -> cookie);
    }

    /**
     * Configure default cookie for all requests
     * ({@link jakarta.ws.rs.client.Invocation.Builder#cookie(jakarta.ws.rs.core.Cookie)}.
     * <p>
     * Use cookie builder: {@code new NewCookie.Builder(name).value(value).build()}.
     * <p>
     * Multiple calls override previous value.
     *
     * @param name     name
     * @param supplier value suppler
     * @return builder instance for chained calls
     * @see jakarta.ws.rs.core.NewCookie
     */
    public T defaultCookie(final String name, final Supplier<Cookie> supplier) {
        defaults.cookie(name, supplier);
        return self();
    }

    /**
     * Configure default Accept header for all requests
     * ({@link jakarta.ws.rs.client.Invocation.Builder#accept(String...)}).
     * <p>
     * Multiple calls override previous value.
     *
     * @param accept media types required for response
     * @return builder instance for chained calls
     * @see jakarta.ws.rs.core.MediaType
     */
    public T defaultAccept(final String... accept) {
        this.defaults.accept(accept);
        return self();
    }

    /**
     * Configure default Accept header for all requests
     * ({@link jakarta.ws.rs.client.Invocation.Builder#accept(String...)}).
     * <p>
     * Multiple calls override previous value.
     *
     * @param accept media types required for response
     * @return builder instance for chained calls
     * @see jakarta.ws.rs.core.MediaType
     */
    public T defaultAccept(final MediaType... accept) {
        final String[] array = Arrays.stream(accept).map(mediaType ->
                        RuntimeDelegate.getInstance().createHeaderDelegate(MediaType.class).toString(mediaType))
                .toArray(String[]::new);
        return defaultAccept(array);
    }

    /**
     * Configure default Accept-Language header for all requests
     * ({@link jakarta.ws.rs.client.Invocation.Builder#acceptLanguage(String...)}).
     * <p>
     * Multiple calls override previous value.
     *
     * @param languages languages to accept
     * @return builder instance for chained calls
     */
    public T defaultLanguage(final String... languages) {
        this.defaults.acceptLanguage(languages);
        return self();
    }

    /**
     * Configure default Accept-Language header for all requests
     * ({@link jakarta.ws.rs.client.Invocation.Builder#acceptLanguage(String...)}).
     * <p>
     * Multiple calls override previous value.
     *
     * @param languages languages to accept
     * @return builder instance for chained calls
     */
    public T defaultLanguage(final Locale... languages) {
        final String[] res = Arrays.stream(languages).map(Locale::toString).toArray(String[]::new);
        return defaultLanguage(res);
    }

    /**
     * Configure default Accept-Encoding header for all requests
     * ({@link jakarta.ws.rs.client.Invocation.Builder#acceptEncoding(String...)}).
     * <p>
     * Multiple calls override previous value.
     *
     * @param encodings encodings to accept
     * @return builder instance for chained calls
     */
    public T defaultEncoding(final String... encodings) {
        this.defaults.acceptEncoding(encodings);
        return self();
    }

    /**
     * Configure default Cache-Control header for all requests
     * ({@link jakarta.ws.rs.client.Invocation.Builder#cacheControl(CacheControl)}).
     * <p>
     * Multiple calls override previous value.
     *
     * @param cacheControl cache control header value (string) to apply to request.
     * @return builder instance for chained calls
     */
    public T defaultCacheControl(final String cacheControl) {
        this.defaults.cacheControl(cacheControl);
        return self();
    }

    /**
     * Configure default Cache-Control header for all requests
     * ({@link jakarta.ws.rs.client.Invocation.Builder#cacheControl(CacheControl)}).
     * <p>
     * Multiple calls override previous value.
     *
     * @param cacheControl cache control settings to apply to request.
     * @return builder instance for chained calls
     */
    public T defaultCacheControl(final CacheControl cacheControl) {
        this.defaults.cacheControl(cacheControl);
        return self();
    }

    /**
     * Enable requests' debugging: print to console what defaults was configured (and where) and how
     * a request object was configured.
     *
     * @param debug true to enable debug
     * @return builder instance for chained calls
     */
    public T defaultDebug(final boolean debug) {
        this.defaults.debug(debug);
        return self();
    }

    /**
     * Configure default request {@link jakarta.ws.rs.client.WebTarget} modifier for all requests.
     *
     * @param modifier function, applying request target configuration
     * @return builder instance for chained calls
     */
    public T defaultPathConfiguration(final Function<WebTarget, WebTarget> modifier) {
        this.defaults.configurePath(modifier);
        return self();
    }

    /**
     * Configure default request {@link jakarta.ws.rs.client.Invocation.Builder} modifier for all requests.
     *
     * @param modifier consumer, applying request configuration
     * @return builder instance for chained calls
     */
    public T defaultRequestConfiguration(final Consumer<Invocation.Builder> modifier) {
        this.defaults.configureRequest(modifier);
        return self();
    }

    /**
     * Could be used for manual appliance of defaults to externally constructed target:
     * {@code Invocation.Builder builder = getDefaults().configure(target)}.
     *
     * @return a copy of client defaults
     */
    public TestRequestConfig getDefaults() {
        return new TestRequestConfig(defaults);
    }

    /**
     * @return if any default configured
     */
    public boolean hasDefaults() {
        return defaults.hasConfiguration();
    }

    /**
     * Could be used for verifications in tests to avoid defaults collide.
     *
     * @return true if default headers specified
     */
    public boolean hasDefaultHeaders() {
        return !defaults.getConfiguredHeaders().isEmpty();
    }

    /**
     * Could be used for verifications in tests to avoid defaults collide.
     *
     * @return true if default query params specified
     */
    public boolean hasDefaultQueryParams() {
        return !defaults.getConfiguredQueryParams().isEmpty();
    }

    /**
     * Could be used for verifications in tests to avoid defaults collide.
     *
     * @return true if default matrix params specified
     */
    public boolean hasDefaultMatrixParams() {
        return !defaults.getConfiguredMatrixParams().isEmpty();
    }

    /**
     * Could be used for verifications in tests to avoid defaults collide.
     *
     * @return true if default path params specified
     */
    public boolean hasDefaultPathParams() {
        return !defaults.getConfiguredPathParams().isEmpty();
    }

    /**
     * Could be used for verifications in tests to avoid defaults collide.
     *
     * @return true if default client properties specified
     */
    public boolean hasDefaultProperties() {
        return !defaults.getConfiguredProperties().isEmpty();
    }

    /**
     * Could be used for verifications in tests to avoid defaults collide.
     *
     * @return true if default client extensions specified
     */
    public boolean hasDefaultExtensions() {
        return !defaults.getConfiguredExtensions().isEmpty();
    }

    /**
     * Could be used for verifications in tests to avoid defaults collide.
     *
     * @return true if cookies specified
     */
    public boolean hasDefaultCookies() {
        return !defaults.getConfiguredCookies().isEmpty();
    }

    /**
     * Could be used for verifications in tests to avoid defaults collide.
     *
     * @return true if default Accept header specified
     */
    public boolean hasDefaultAccepts() {
        return !defaults.getConfiguredAccepts().isEmpty();
    }

    /**
     * Could be used for verifications in tests to avoid defaults collide.
     *
     * @return true if default Accept-Language header specified
     */
    public boolean hasDefaultLanguages() {
        return !defaults.getConfiguredLanguages().isEmpty();
    }

    /**
     * Could be used for verifications in tests to avoid defaults collide.
     *
     * @return true if default Accept-Encoding header specified
     */
    public boolean hasDefaultEncodings() {
        return !defaults.getConfiguredEncodings().isEmpty();
    }

    /**
     * Could be used for verifications in tests to avoid defaults collide.
     *
     * @return true if default Cache header specified
     */
    public boolean hasDefaultCacheControl() {
        return defaults.getConfiguredCacheControl() != null;
    }

    /**
     * Could be used for verifications in tests to avoid defaults collide.
     *
     * @return true if default date formatter (either one or both) specified for form builder
     * @see #defaultFormDateFormatter(java.text.DateFormat)
     * @see #defaultFormDateTimeFormatter(java.time.format.DateTimeFormatter)
     */
    public boolean hasDefaultFormDateFormatter() {
        return defaults.getConfiguredFormDateFormatter() != null
                || defaults.getConfiguredFormDateTimeFormatter() != null;
    }

    /**
     * Could be used for verifications in tests to avoid defaults collide.
     *
     * @return true if default path or request customizers registered
     * @see #defaultPathConfiguration(java.util.function.Function)
     * @see #defaultRequestConfiguration(java.util.function.Consumer)
     */
    public boolean hasDefaultCustomConfigurators() {
        return !defaults.getConfiguredPathModifiers().isEmpty()
                || !defaults.getConfiguredRequestModifiers().isEmpty();
    }

    /**
     * @return true if debug output is enabled
     * @see #defaultDebug(boolean)
     */
    public boolean isDebugEnabled() {
        return defaults.isDebugEnabled();
    }

    /**
     * Reset configured defaults.
     *
     * @return rest client itself for chained calls
     */
    public T reset() {
        defaults.clear();
        return self();
    }

    /**
     * Print configured defaults to console.
     *
     * @return builder instance for chained calls
     */
    @SuppressWarnings("PMD.SystemPrintln")
    public T printDefaults() {
        System.out.println(defaults.printConfiguration());
        return self();
    }

    /**
     * @return client itself
     */
    @SuppressWarnings("unchecked")
    protected T self() {
        return (T) this;
    }
}
