package ru.vyarus.dropwizard.guice.test.client.builder;

import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.ext.RuntimeDelegate;
import org.jspecify.annotations.Nullable;
import ru.vyarus.dropwizard.guice.test.client.builder.track.RequestTracker;
import ru.vyarus.dropwizard.guice.test.client.builder.util.RequestModifierSource;
import ru.vyarus.dropwizard.guice.test.client.builder.util.TestRequestConfigPrinter;
import ru.vyarus.dropwizard.guice.test.client.builder.util.conf.JerseyRequestConfigurer;
import ru.vyarus.dropwizard.guice.test.client.util.SourceAwareValue;

import java.text.DateFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Request configuration. Used to configure client defaults and for exact request configuration.
 * The main problem with the jersey client api is that it split into two phases: request target (including
 * query params) and request itself. So we have to collect all required data separately to apply both phases
 * at once (to provide simpler api).
 * <p>
 * The configuration is hierarchical: sub-clients could inherit top level configurations (defaults copies on
 * creation).
 * <p>
 * Most properties provide {@link Supplier}-based configuration variant for lazy evaluation (applicable for defaults,
 * when actual value is resolved in time of request creation and not in time of default registration).
 * <p>
 * Also provides direct access for {@link javax.ws.rs.client.WebTarget} and
 * {@link javax.ws.rs.client.Invocation.Builder} objects with {@link #configurePath(java.util.function.Function)} and
 * {@link #configureRequest(java.util.function.Consumer)} methods. Config itself is registered in these methods
 * to apply all collected data.
 * <p>
 * Object also keeps data formatters to be used in form builder.
 *
 * @author Vyacheslav Rusakov
 * @since 13.09.2025
 */
@SuppressWarnings({"PMD.GodClass", "PMD.TooManyMethods", "PMD.ExcessivePublicCount", "PMD.CouplingBetweenObjects",
        "PMD.CyclomaticComplexity", "checkstyle:OverloadMethodsDeclarationOrder"})
public class TestRequestConfig implements Function<WebTarget, WebTarget>, Consumer<Invocation.Builder> {

    // suppliers used for "defaults" case
    private final Map<String, SourceAwareValue<Object>> queryParams = new LinkedHashMap<>();
    private final Map<String, SourceAwareValue<Object>> matrixParams = new LinkedHashMap<>();
    private final Map<String, SourceAwareValue<Object>> pathParams = new LinkedHashMap<>();
    private final Map<String, SourceAwareValue<Object>> headers = new LinkedHashMap<>();
    private final Map<String, SourceAwareValue<Cookie>> cookies = new LinkedHashMap<>();
    private final Map<String, SourceAwareValue<Object>> properties = new LinkedHashMap<>();
    // jersey extensions registration (normally registered with .register())
    private final Map<Class<?>, SourceAwareValue<?>> extensions = new LinkedHashMap<>();

    private final List<SourceAwareValue<Function<WebTarget, WebTarget>>> pathModifiers = new ArrayList<>();
    private final List<SourceAwareValue<Consumer<Invocation.Builder>>> requestModifiers = new ArrayList<>();

    private SourceAwareValue<String[]> acceptHeader;
    private SourceAwareValue<String[]> languageHeader;
    private SourceAwareValue<String[]> encondingHeader;
    private SourceAwareValue<CacheControl> cache;
    // used for form fields serialization
    private SourceAwareValue<DateFormat> dateFormatter;
    private SourceAwareValue<DateTimeFormatter> dateTimeFormatter;
    private boolean debugEnabled;
    private Consumer<RequestTracker> requestAssertion;

    /**
     * Create a request config.
     *
     * @param base base configuration (optional) to copy values from
     */
    @SuppressWarnings("PMD.ConstructorCallsOverridableMethod")
    public TestRequestConfig(final @Nullable TestRequestConfig base) {
        configurePath(this);
        configureRequest(this);
        if (base != null) {
            copy(base);
        }
    }

    /**
     * Provides direct access for jersey {@link javax.ws.rs.client.WebTarget} object configuration.
     *
     * @param modifier function, applying request target configuration
     * @return builder instance for chained calls
     */
    public TestRequestConfig configurePath(final Function<WebTarget, WebTarget> modifier) {
        pathModifiers.add(value(() -> modifier));
        return this;
    }

    /**
     * Provides direct access for jersey {@link javax.ws.rs.client.Invocation.Builder} object configuration.
     *
     * @param modifier consumer, applying request configuration
     * @return builder instance for chained calls
     */
    public TestRequestConfig configureRequest(final Consumer<Invocation.Builder> modifier) {
        requestModifiers.add(value(() -> modifier));
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
    public TestRequestConfig property(final String name, final Object value) {
        return property(name, () -> value);
    }

    /**
     * Configure jersey client property configuration
     * ({@link org.glassfish.jersey.client.JerseyClient#property(String, Object)}).
     * <p>
     * Multiple calls override previous value.
     *
     * @param name     property name
     * @param supplier value supplier
     * @return builder instance for chained calls
     * @see org.glassfish.jersey.client.ClientProperties
     */
    public TestRequestConfig property(final String name, final Supplier<Object> supplier) {
        properties.put(name, value(supplier));
        return this;
    }

    /**
     * Configure jersey client extension registration
     * ({@link org.glassfish.jersey.client.JerseyClient#register(Class)}).
     * Could be useful, for example, to register custom {@link javax.ws.rs.ext.MessageBodyReader}.
     * <p>
     * Multiple registrations of the same class will be ignored (extensions tracked by type).
     *
     * @param type extension class
     * @param <K>  extension type
     * @return builder instance for chained calls
     */
    @SuppressWarnings("unchecked")
    public <K> TestRequestConfig register(final Class<K> type) {
        return register(type, () -> (K) type);
    }

    /**
     * Configure jersey client extension registration
     * ({@link org.glassfish.jersey.client.JerseyClient#register(Object)}).
     * Could be useful, for example, to register custom {@link javax.ws.rs.ext.MessageBodyReader} (as instance).
     * <p>
     * When called with different instances of the same class: only the latest registration will be used
     * (extensions tracked by type).
     *
     * @param extension extensions instance
     * @param <K>       extension type
     * @return builder instance for chained calls
     */
    @SuppressWarnings("unchecked")
    public <K> TestRequestConfig register(final K extension) {
        return register((Class<K>) extension.getClass(), () -> extension);
    }

    /**
     * Configure jersey client extension registration
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
    public <K> TestRequestConfig register(final Class<K> type, final @Nullable Supplier<K> supplier) {
        extensions.put(type, value(supplier));
        return this;
    }

    /**
     * Configure request Accept header ({@link javax.ws.rs.client.Invocation.Builder#accept(String...)}).
     * <p>
     * Multiple calls override previous value.
     *
     * @param accept media types required for response
     * @return builder instance for chained calls
     * @see javax.ws.rs.core.MediaType
     */
    public TestRequestConfig accept(final String... accept) {
        this.acceptHeader = value(() -> accept);
        return this;
    }

    /**
     * Configure request Accept-Language header
     * ({@link javax.ws.rs.client.Invocation.Builder#acceptLanguage(String...)}).
     * <p>
     * Multiple calls override previous value.
     *
     * @param language languages to accept
     * @return builder instance for chained calls
     */
    public TestRequestConfig acceptLanguage(final String... language) {
        this.languageHeader = value(() -> language);
        return this;
    }

    /**
     * Configure request Accept-Encoding header
     * ({@link javax.ws.rs.client.Invocation.Builder#acceptEncoding(String...)}).
     * <p>
     * Multiple calls override previous value.
     *
     * @param encoding encodings to accept
     * @return builder instance for chained calls
     */
    public TestRequestConfig acceptEncoding(final String... encoding) {
        this.encondingHeader = value(() -> encoding);
        return this;
    }

    /**
     * Configure request Cache-Control header
     * ({@link javax.ws.rs.client.Invocation.Builder#cacheControl(CacheControl)}).
     * <p>
     * Multiple calls override previous value.
     *
     * @param cacheControl cache control header value (as string).
     * @return builder instance for chained calls
     */
    public TestRequestConfig cacheControl(final String cacheControl) {
        return cacheControl(RuntimeDelegate.getInstance().createHeaderDelegate(CacheControl.class)
                .fromString(cacheControl));
    }

    /**
     * Configure request Cache-Control header
     * ({@link javax.ws.rs.client.Invocation.Builder#cacheControl(CacheControl)}).
     * <p>
     * Multiple calls override previous value.
     *
     * @param cacheControl cache control settings to apply to request.
     * @return builder instance for chained calls
     */
    public TestRequestConfig cacheControl(final CacheControl cacheControl) {
        this.cache = cacheControl == null ? null : value(() -> cacheControl);
        return this;
    }

    /**
     * Configure request query parameter ({@link javax.ws.rs.client.WebTarget#queryParam(String, Object...)}).
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
    public TestRequestConfig queryParam(final String name, final Object value) {
        return queryParam(name, () -> value);
    }


    /**
     * Configure request query parameter ({@link javax.ws.rs.client.WebTarget#queryParam(String, Object...)}).
     * <p>
     * Note: jersey api supports multiple query parameters with the same name (in this case multiple parameters
     * added). The same result could be achieved by passing list or array into this api.
     * <p>
     * Multiple calls with the same name override the previous value (the only way to specify multiple values is
     * using list or array as value).
     *
     * @param name     param name
     * @param supplier value supplier (list or array for multiple values)
     * @return builder instance for chained calls
     */
    public TestRequestConfig queryParam(final String name, final Supplier<Object> supplier) {
        this.queryParams.put(name, value(supplier));
        return this;
    }

    /**
     * Configure request path parameter
     * ({@link javax.ws.rs.client.WebTarget#resolveTemplateFromEncoded(String, Object)}).
     * <p>
     * Note: parameter value assumed to be encoded to be able to specify matrix parameters (in the middle of the path):
     * {@code pathParam("var", "/path;var=1;val=2")}.
     *
     * @param name  parameter name
     * @param value parameter value
     * @return builder instance for chained calls
     */
    public TestRequestConfig pathParam(final String name, final Object value) {
        return pathParam(name, () -> value);
    }

    /**
     * Configure request path parameter
     * ({@link javax.ws.rs.client.WebTarget#resolveTemplateFromEncoded(String, Object)}).
     *
     * @param name     parameter name
     * @param supplier parameter value supplier
     * @return builder instance for chained calls
     */
    public TestRequestConfig pathParam(final String name, final Supplier<Object> supplier) {
        this.pathParams.put(name, value(supplier));
        return this;
    }

    /**
     * Configure request matrix parameter ({@link javax.ws.rs.client.WebTarget#matrixParam(String, Object...)}).
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
    public TestRequestConfig matrixParam(final String name, final Object value) {
        return matrixParam(name, () -> value);
    }


    /**
     * Configure request matrix parameter ({@link javax.ws.rs.client.WebTarget#matrixParam(String, Object...)}).
     * <p>
     * Note: jersey api supports multiple matrix parameters with the same name (in this case multiple parameters
     * added). The same result could be achieved by passing list or array into this api.
     * <p>
     * Multiple calls with the same name override the previous value (the only way to specify multiple values is
     * using list or array as value).
     *
     * @param name     param name
     * @param supplier value supplier (list or array for multiple values)
     * @return builder instance for chained calls
     */
    public TestRequestConfig matrixParam(final String name, final Supplier<Object> supplier) {
        this.matrixParams.put(name, value(supplier));
        return this;
    }

    /**
     * Configure request header ({@link javax.ws.rs.client.Invocation.Builder#header(String, Object)}).
     * <p>
     * Multiple calls override previous value.
     *
     * @param name  header name
     * @param value header value
     * @return builder instance for chained calls
     */
    public TestRequestConfig header(final String name, final Object value) {
        return header(name, () -> value);
    }

    /**
     * Configure request header ({@link javax.ws.rs.client.Invocation.Builder#header(String, Object)}).
     * <p>
     * Multiple calls override previous value.
     *
     * @param name     header name
     * @param supplier value supplier
     * @return builder instance for chained calls
     */
    public TestRequestConfig header(final String name, final Supplier<Object> supplier) {
        this.headers.put(name, value(supplier));
        return this;
    }

    /**
     * Configure request cookie ({@link javax.ws.rs.client.Invocation.Builder#cookie(javax.ws.rs.core.Cookie)}.
     * <p>
     * Multiple calls override previous value.
     *
     * @param cookie cookie value
     * @return builder instance for chained calls
     * @see javax.ws.rs.core.NewCookie
     */
    public TestRequestConfig cookie(final Cookie cookie) {
        return cookie(cookie.getName(), () -> cookie);
    }

    /**
     * Configure request cookie ({@link javax.ws.rs.client.Invocation.Builder#cookie(javax.ws.rs.core.Cookie)}.
     * <p>
     * Multiple calls override previous value.
     *
     * @param name     name
     * @param supplier value suppler
     * @return builder instance for chained calls
     * @see javax.ws.rs.core.NewCookie
     */
    public TestRequestConfig cookie(final String name, final Supplier<Cookie> supplier) {
        this.cookies.put(name, value(supplier));
        return this;
    }

    /**
     * Configure java.util date formatter for form fields (used in
     * {@link ru.vyarus.dropwizard.guice.test.client.builder.FormBuilder}).
     *
     * @param formatter date formatter
     * @return builder instance for chained calls
     */
    public TestRequestConfig formDateFormatter(final DateFormat formatter) {
        this.dateFormatter = value(() -> formatter);
        return this;
    }

    /**
     * Configure java.time formatter for form fields (used in
     * {@link ru.vyarus.dropwizard.guice.test.client.builder.FormBuilder}).
     *
     * @param formatter date formatter
     * @return builder instance for chained calls
     */
    public TestRequestConfig formDateTimeFormatter(final DateTimeFormatter formatter) {
        this.dateTimeFormatter = value(() -> formatter);
        return this;
    }

    /**
     * Enable debug output for configured requests.
     *
     * @param debug true to enable debug, false to disable
     * @return builder instance for chained calls
     */
    public TestRequestConfig debug(final boolean debug) {
        this.debugEnabled = debug;
        return this;
    }

    /**
     * Assert configuration applied to {@link javax.ws.rs.client.WebTarget} and
     * {@link javax.ws.rs.client.Invocation.Builder}.
     * <p>
     * Option implicitly enables debug.
     * <p>
     * Assertions would be executed just before request execution
     *
     * @param assertion assertions
     * @return builder instance for chained calls
     */
    public TestRequestConfig assertRequest(final Consumer<RequestTracker> assertion) {
        debug(true);
        this.requestAssertion = assertion;
        return this;
    }

    /**
     * Build jersey client request with configured values.
     *
     * @param webTarget target
     * @return pre-configured jersey client request builder
     */
    @SuppressWarnings("PMD.SystemPrintln")
    public Invocation.Builder applyRequestConfiguration(final WebTarget webTarget) {
        WebTarget target = webTarget;
        if (debugEnabled) {
            final RequestTracker tracker = new RequestTracker();
            target = tracker.track(webTarget, () -> {
                System.out.println("Request configuration: \n" + printConfiguration());
                System.out.println("Jersey request configuration: \n" + tracker.getLog());
                if (requestAssertion != null) {
                    requestAssertion.accept(tracker);
                }
            });
        }
        for (Supplier<Function<WebTarget, WebTarget>> fun : pathModifiers) {
            target = fun.get().apply(target);
        }
        final Invocation.Builder request = target.request();
        requestModifiers.forEach(fun -> fun.get().accept(request));
        return request;
    }

    /**
     * Clear configuration.
     *
     * @return builder instance for chained calls
     */
    public TestRequestConfig clear() {
        queryParams.clear();
        matrixParams.clear();
        pathParams.clear();
        headers.clear();
        cookies.clear();
        properties.clear();
        extensions.clear();
        acceptHeader = null;
        languageHeader = null;
        encondingHeader = null;
        pathModifiers.subList(1, pathModifiers.size()).clear();
        requestModifiers.subList(1, requestModifiers.size()).clear();
        dateFormatter = null;
        dateTimeFormatter = null;
        cache = null;
        debugEnabled = false;
        requestAssertion = null;
        return this;
    }

    /**
     * @return true if any configuration applied, false otherwise
     */
    @SuppressWarnings({"checkstyle:CyclomaticComplexity", "checkstyle:BooleanExpressionComplexity"})
    public boolean hasConfiguration() {
        return !queryParams.isEmpty()
                || !matrixParams.isEmpty()
                || !pathParams.isEmpty()
                || !headers.isEmpty()
                || !cookies.isEmpty()
                || !properties.isEmpty()
                || !extensions.isEmpty()
                || acceptHeader != null
                || languageHeader != null
                || encondingHeader != null
                // config is a modifier itself
                || pathModifiers.size() > 1
                || requestModifiers.size() > 1
                || dateFormatter != null
                || dateTimeFormatter != null
                || cache != null;
    }

    /**
     * @return render configured values as string
     */
    public String printConfiguration() {
        return TestRequestConfigPrinter.print(this);
    }

    /**
     * @return configured query parameter names or empty set if not configured
     */
    public Set<String> getConfiguredQueryParams() {
        return queryParams.keySet();
    }

    /**
     * @return configured query parameters map or empty map if not configured
     */
    public Map<String, Object> getConfiguredQueryParamsMap() {
        final Map<String, Object> res = new HashMap<>();
        queryParams.forEach((key, value) -> res.put(key, value.get()));
        return res;
    }

    /**
     * @return query parameters with declaration sources or empty map if not configured
     */
    public Map<String, SourceAwareValue<Object>> getConfiguredQueryParamsSource() {
        return queryParams;
    }

    /**
     * @return configured matrix parameter names or empty set if not configured
     */
    public Set<String> getConfiguredMatrixParams() {
        return matrixParams.keySet();
    }

    /**
     * @return configured matrix parameters map or empty map if not configured
     */
    public Map<String, Object> getConfiguredMatrixParamsMap() {
        final Map<String, Object> res = new HashMap<>();
        matrixParams.forEach((key, value) -> res.put(key, value.get()));
        return res;
    }

    /**
     * @return matrix parameters with declaration sources or empty map if not configured
     */
    public Map<String, SourceAwareValue<Object>> getConfiguredMatrixParamsSource() {
        return matrixParams;
    }

    /**
     * @return configured header names or empty set if not configured
     */
    public Set<String> getConfiguredHeaders() {
        return headers.keySet();
    }

    /**
     * @return configured headers map or empty map if not configured
     */
    public Map<String, Object> getConfiguredHeadersMap() {
        final Map<String, Object> res = new HashMap<>();
        headers.forEach((key, value) -> res.put(key, value.get()));
        return res;
    }

    /**
     * @return configured headers with declaration sources or empty map if not configured
     */
    public Map<String, SourceAwareValue<Object>> getConfiguredHeadersSource() {
        return headers;
    }

    /**
     * @return configured cookie names or empty set if not configured
     */
    public Set<String> getConfiguredCookies() {
        return cookies.keySet();
    }

    /**
     * @return configured cookies map or empty map if not configured
     */
    public Map<String, Cookie> getConfiguredCookiesMap() {
        final Map<String, Cookie> res = new HashMap<>();
        cookies.forEach((key, value) -> res.put(key, value.get()));
        return res;
    }

    /**
     * @return configured cookie with declaration sources or empty map if not configured
     */
    public Map<String, SourceAwareValue<Cookie>> getConfiguredCookiesSource() {
        return cookies;
    }

    /**
     * @return configured property names or empty set if not configured
     */
    public Set<String> getConfiguredProperties() {
        return properties.keySet();
    }

    /**
     * @return configured properties map or empty map if not configured
     */
    public Map<String, Object> getConfiguredPropertiesMap() {
        final Map<String, Object> res = new HashMap<>();
        properties.forEach((key, value) -> res.put(key, value.get()));
        return res;
    }

    /**
     * @return configured properties with declaration sources or empty map if not configured.
     */
    public Map<String, SourceAwareValue<Object>> getConfiguredPropertiesSource() {
        return properties;
    }

    /**
     * @return configured extension classes or empty set if not configured.
     */
    public Set<Class<?>> getConfiguredExtensions() {
        return extensions.keySet();
    }

    /**
     * Note: extensions registered with class will have class in value.
     *
     * @return configured extensions map or empty map if not configured
     */
    public Map<Class<?>, Object> getConfiguredExtensionsMap() {
        final Map<Class<?>, Object> res = new HashMap<>();
        extensions.forEach((key, value) -> res.put(key, value.get()));
        return res;
    }

    /**
     * @return configured extensions with declaration sources or empty map if not configured
     */
    public Map<Class<?>, SourceAwareValue<?>> getConfiguredExtensionsSource() {
        return extensions;
    }

    /**
     * @return configured path param names or empty set if not configured
     */
    public Set<String> getConfiguredPathParams() {
        return pathParams.keySet();
    }

    /**
     * @return configured path params map or empty map if not configured
     */
    public Map<String, Object> getConfiguredPathParamsMap() {
        final Map<String, Object> res = new HashMap<>();
        pathParams.forEach((key, value) -> res.put(key, value.get()));
        return res;
    }

    /**
     * @return configured path params with declaration sources or empty map if not configured
     */
    public Map<String, SourceAwareValue<Object>> getConfiguredPathParamsSource() {
        return pathParams;
    }

    /**
     * @return configured accept media types or empty list if not configured
     */
    public List<String> getConfiguredAccepts() {
        return acceptHeader == null ? Collections.emptyList() : Arrays.asList(acceptHeader.get());
    }

    /**
     * @return configured media types with declaration source or null if not configured
     */
    @Nullable
    public SourceAwareValue<String[]> getConfiguredAcceptsSource() {
        return acceptHeader;
    }

    /**
     * @return configured languages or empty list if not configured
     */
    public List<String> getConfiguredLanguages() {
        return languageHeader == null ? Collections.emptyList() : Arrays.asList(languageHeader.get());
    }

    /**
     * @return configured languages with declaration source or null if not configured
     */
    @Nullable
    public SourceAwareValue<String[]> getConfiguredLanguagesSource() {
        return languageHeader;
    }

    /**
     * @return configured accept languages or empty list if not configured
     */
    public List<String> getConfiguredEncodings() {
        return encondingHeader == null ? Collections.emptyList() : Arrays.asList(encondingHeader.get());
    }

    /**
     * @return configured encodings with declaration source or null if not configured
     */
    @Nullable
    public SourceAwareValue<String[]> getConfiguredEncodingsSource() {
        return encondingHeader;
    }

    /**
     * @return configured java.util date formatter or null if not configured
     */
    @Nullable
    public DateFormat getConfiguredFormDateFormatter() {
        return dateFormatter == null ? null : dateFormatter.get();
    }

    /**
     * @return configured java.util date formatter supplier with declaration source or null if not configured
     */
    @Nullable
    public SourceAwareValue<DateFormat> getConfiguredFormDateFormatterSource() {
        return dateFormatter;
    }

    /**
     * @return configured java.time date formatter or null if not configured
     */
    @Nullable
    public DateTimeFormatter getConfiguredFormDateTimeFormatter() {
        return dateTimeFormatter == null ? null : dateTimeFormatter.get();
    }

    /**
     * @return configured java.time date formatter supplier with declaration source or null if not configured
     */
    @Nullable
    public SourceAwareValue<DateTimeFormatter> getConfiguredFormDateTimeFormatterSource() {
        return dateTimeFormatter;
    }

    /**
     * @return configured cache control or null if not configured
     */
    @Nullable
    public CacheControl getConfiguredCacheControl() {
        return cache == null ? null : cache.get();
    }

    /**
     * @return configured cache control with declaration source or null
     */
    @Nullable
    public SourceAwareValue<CacheControl> getConfiguredCacheControlSource() {
        return cache;
    }

    /**
     * @return custom path modifiers (excluding config object itself)
     */
    public List<Function<WebTarget, WebTarget>> getConfiguredPathModifiers() {
        return pathModifiers.size() == 1 ? Collections.emptyList()
                : pathModifiers.subList(1, pathModifiers.size())
                .stream().map(SourceAwareValue::get)
                .collect(Collectors.toList());
    }

    /**
     * @return configured path modifiers with declaration sources or empty list
     */
    public List<? extends SourceAwareValue<Function<WebTarget, WebTarget>>> getConfiguredPathModifiersSource() {
        return pathModifiers.size() == 1 ? Collections.emptyList()
                : pathModifiers.subList(1, pathModifiers.size());
    }

    /**
     * @return custom request modifiers (excluding config object itself)
     */
    public List<Consumer<Invocation.Builder>> getConfiguredRequestModifiers() {
        return requestModifiers.size() == 1 ? Collections.emptyList()
                : requestModifiers.subList(1, requestModifiers.size())
                .stream().map(SourceAwareValue::get)
                .collect(Collectors.toList());
    }

    /**
     * @return configured request modifiers with declaration sources or empty list
     */
    public List<? extends SourceAwareValue<Consumer<Invocation.Builder>>> getConfiguredRequestModifiersSource() {
        return requestModifiers.size() == 1 ? Collections.emptyList()
                : requestModifiers.subList(1, requestModifiers.size());
    }

    /**
     * @return true if debug enabled
     */
    public boolean isDebugEnabled() {
        return debugEnabled;
    }

    /**
     * Applies target configuration. Used under {@link #configurePath(java.util.function.Function)}.
     * <p>
     * Implicitly called by {@link #applyRequestConfiguration(javax.ws.rs.client.WebTarget)}.
     *
     * @param webTarget target to configure
     * @return configured target
     */
    @Override
    public WebTarget apply(final WebTarget webTarget) {
        WebTarget target = webTarget;
        if (!pathParams.isEmpty()) {
            target = target.resolveTemplatesFromEncoded(getConfiguredPathParamsMap());
        }
        if (!queryParams.isEmpty()) {
            target = JerseyRequestConfigurer.applyQueryParams(target, queryParams);
        }
        if (!matrixParams.isEmpty()) {
            target = JerseyRequestConfigurer.applyMatrixParams(target, matrixParams);
        }
        if (!properties.isEmpty()) {
            for (Map.Entry<String, ? extends Supplier<Object>> entry : properties.entrySet()) {
                target.property(entry.getKey(), entry.getValue().get());
            }
        }
        if (!extensions.isEmpty()) {
            JerseyRequestConfigurer.applyExtensions(target, extensions);
        }

        return target;
    }

    /**
     * Applies builder configurations. Used under {@link #configureRequest(java.util.function.Consumer)}.
     * <p>
     * Implicitly called by {@link #applyRequestConfiguration(javax.ws.rs.client.WebTarget)}.
     *
     * @param request builder to configure.
     */
    @Override
    public void accept(final Invocation.Builder request) {
        if (acceptHeader != null) {
            request.accept(acceptHeader.get());
        }
        if (languageHeader != null) {
            request.acceptLanguage(languageHeader.get());
        }
        if (encondingHeader != null) {
            request.acceptEncoding(encondingHeader.get());
        }
        if (!headers.isEmpty()) {
            headers.forEach((name, value) -> request.header(name, value.get()));
        }
        if (!cookies.isEmpty()) {
            cookies.values().forEach(cookie -> request.cookie(cookie.get()));
        }
        if (cache != null) {
            request.cacheControl(cache.get());
        }
    }

    private void copy(final TestRequestConfig base) {
        queryParams.putAll(base.queryParams);
        matrixParams.putAll(base.matrixParams);
        pathParams.putAll(base.pathParams);
        headers.putAll(base.headers);
        cookies.putAll(base.cookies);
        properties.putAll(base.properties);
        extensions.putAll(base.extensions);
        acceptHeader = base.acceptHeader;
        languageHeader = base.languageHeader;
        encondingHeader = base.encondingHeader;
        if (base.pathModifiers.size() > 1) {
            pathModifiers.addAll(base.pathModifiers.subList(1, base.pathModifiers.size()));
        }
        if (base.requestModifiers.size() > 1) {
            requestModifiers.addAll(base.requestModifiers.subList(1, base.requestModifiers.size()));
        }
        dateFormatter = base.dateFormatter;
        dateTimeFormatter = base.dateTimeFormatter;
        cache = base.cache;
        debugEnabled = base.debugEnabled;
        requestAssertion = base.requestAssertion;
    }

    private <T> SourceAwareValue<T> value(final Supplier<T> supplier) {
        return new SourceAwareValue<>(supplier, RequestModifierSource.getSource());
    }
}
