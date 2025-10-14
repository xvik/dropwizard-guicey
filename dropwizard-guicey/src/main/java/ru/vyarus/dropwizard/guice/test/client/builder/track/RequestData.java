package ru.vyarus.dropwizard.guice.test.client.builder.track;

import com.google.common.collect.ImmutableList;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.CacheControl;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.RuntimeDelegate;
import org.jspecify.annotations.Nullable;
import ru.vyarus.dropwizard.guice.debug.util.RenderUtils;
import ru.vyarus.dropwizard.guice.module.installer.util.StackUtils;
import ru.vyarus.dropwizard.guice.test.client.ResourceClient;
import ru.vyarus.dropwizard.guice.test.client.TestClient;
import ru.vyarus.dropwizard.guice.test.client.TestRestClient;
import ru.vyarus.dropwizard.guice.test.client.builder.TestClientDefaults;
import ru.vyarus.dropwizard.guice.test.client.builder.TestClientRequestBuilder;
import ru.vyarus.dropwizard.guice.test.client.builder.track.impl.BuilderTracker;
import ru.vyarus.dropwizard.guice.test.client.builder.track.impl.TargetTracker;
import ru.vyarus.dropwizard.guice.test.client.builder.track.impl.UriBuilderTracker;
import ru.vyarus.dropwizard.guice.test.client.builder.util.RequestModifierSource;
import ru.vyarus.dropwizard.guice.test.client.util.SourceAwareValue;
import ru.vyarus.java.generics.resolver.util.TypeToStringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Recorder request configuration data. Each applied value is recorded with an application source (line).
 * <p>
 * All values tracked with the recorded configuration source (source line, called method), that's why all values are
 * wrapped with {@link ru.vyarus.dropwizard.guice.test.client.util.SourceAwareValue}.
 *
 * @author Vyacheslav Rusakov
 * @since 04.10.2025
 */
@SuppressWarnings({"PMD.GodClass", "PMD.AvoidFieldNameMatchingMethodName", "PMD.TooManyMethods"})
public class RequestData {
    private static final List<Class<?>> INFRA = ImmutableList.of(
            TestClientRequestBuilder.class,
            TestClientDefaults.class,
            TestClient.class,
            TestRestClient.class,
            ResourceClient.class,
            RequestModifierSource.class,
            TargetTracker.class,
            BuilderTracker.class,
            UriBuilderTracker.class,
            RequestData.class
    );
    private static final String DBL_TAB = "\t\t";

    private final Runnable preRequestAction;

    @SuppressWarnings("PMD.AvoidStringBufferField")
    private final StringBuilder log = new StringBuilder();

    private final Map<String, SourceAwareValue<Object>> queryParams = new LinkedHashMap<>();
    private final Map<String, SourceAwareValue<Object>> matrixParams = new LinkedHashMap<>();
    private final List<SourceAwareValue<PathParam>> pathParams = new ArrayList<>();
    private final Map<String, SourceAwareValue<Object>> headers = new LinkedHashMap<>();
    private final Map<String, SourceAwareValue<Cookie>> cookies = new LinkedHashMap<>();
    private final Map<String, SourceAwareValue<Object>> properties = new LinkedHashMap<>();
    private final Map<Class<?>, SourceAwareValue<Extension>> extensions = new LinkedHashMap<>();
    private final List<SourceAwareValue<String>> paths = new ArrayList<>();

    private SourceAwareValue<String[]> acceptHeader;
    private SourceAwareValue<String[]> languageHeader;
    private SourceAwareValue<String[]> encodingHeader;
    private SourceAwareValue<CacheControl> cache;

    private SourceAwareValue<String> method;
    private SourceAwareValue<Entity<?>> entity;
    private SourceAwareValue<GenericType<?>> resultMapping;

    private String url;

    /**
     * Create a request data object.
     *
     * @param preRequestAction action to be performed before request execution (e.g. logging)
     */
    public RequestData(@Nullable final Runnable preRequestAction) {
        this.preRequestAction = preRequestAction;
    }

    // -------------------------------------------------------------- SETTERS

    /**
     * Record path configuration {@link jakarta.ws.rs.client.WebTarget#path(String)}.
     *
     * @param path provided path
     */
    public void path(final String path) {
        final SourceAwareValue<String> value = value(path);
        paths.add(value);
        trace("Path", path, value.getSource());
    }

    /**
     * Record path param {@link jakarta.ws.rs.client.WebTarget#resolveTemplate(String, Object)}
     * (and all other variations).
     *
     * @param name              param name
     * @param value             param value
     * @param encodeSlashInPath true if slash encoding required
     * @param encoded           true if value already encoded
     */
    public void resolveTemplate(final String name, final Object value,
                                final boolean encodeSlashInPath, final boolean encoded) {
        final SourceAwareValue<PathParam> valueSource = value(new PathParam(name, value, encodeSlashInPath, encoded));
        pathParams.add(valueSource);
        trace("Resolve template", "(encodeSlashInPath=" + encodeSlashInPath
                + " encoded=" + encoded + ")\n\t\t" + name + "=" + value, valueSource.getSource());
    }

    /**
     * Record multiple path params {@link jakarta.ws.rs.client.WebTarget#resolveTemplates(java.util.Map)}
     * (and all other variations).
     *
     * @param value             path parameters
     * @param encodeSlashInPath true if slash encoding required
     * @param encoded           true if values already encoded
     */
    public void resolveTemplates(final Map<String, Object> value, final boolean encodeSlashInPath,
                                 final boolean encoded) {
        final String source = getCallSource();
        value.forEach((s, o) -> pathParams.add(
                new SourceAwareValue<>(() -> new PathParam(s, o, encodeSlashInPath, encoded), source)));
        trace("Resolve template", "(encodeSlashInPath=" + encodeSlashInPath + " encoded=" + encoded + ")\n"
                + toStringMap(value, DBL_TAB), source);
    }

    /**
     * Record matric param {@link jakarta.ws.rs.client.WebTarget#matrixParam(String, Object...)}.
     *
     * @param name  param name
     * @param value param values
     */
    public void matrixParam(final String name, final Object... value) {
        final SourceAwareValue<Object> valueSource = value(value.length == 1 ? value[0] : value);
        matrixParams.put(name, valueSource);
        trace("Matrix param", name + "=" + (value.length == 1 ? value[0] : Arrays.toString(value)),
                valueSource.getSource());
    }

    /**
     * Record query param {@link jakarta.ws.rs.client.WebTarget#queryParam(String, Object...)}.
     *
     * @param name  param name
     * @param value param values
     */
    public void queryParam(final String name, final Object... value) {
        final SourceAwareValue<Object> valueSource = value(value.length == 1 ? value[0] : value);
        queryParams.put(name, valueSource);
        trace("Query param", name + "=" + (value.length == 1 ? value[0] : Arrays.toString(value)),
                valueSource.getSource());
    }

    /**
     * Expected response type {@link jakarta.ws.rs.client.WebTarget#request(String...)},
     * {@link jakarta.ws.rs.client.Invocation.Builder#accept(String...)}.
     *
     * @param value response types
     */
    public void accept(final String... value) {
        acceptHeader = value(value);
        trace("Accept", Arrays.toString(value), acceptHeader.getSource());
    }

    /**
     * Record property {@link jakarta.ws.rs.client.WebTarget#property(String, Object)},
     * {@link jakarta.ws.rs.client.Invocation.Builder#property(String, Object)}.
     *
     * @param name  property name
     * @param value property value
     */
    public void property(final String name, final Object value) {
        final SourceAwareValue<Object> valueSource = value(value);
        properties.put(name, valueSource);
        trace("Property", name + "=" + value, valueSource.getSource());
    }

    /**
     * Record extension (by class or instance) {@link jakarta.ws.rs.client.WebTarget#register(Class)}.
     *
     * @param value     extension class or instance
     * @param priority  priority
     * @param contracts constracts
     */
    public void register(final Object value, final int priority, final Class<?>... contracts) {
        final Class<?> ext = value instanceof Class ? (Class<?>) value : value.getClass();
        final SourceAwareValue<Extension> valueSource = value(new Extension(ext, value, priority, contracts));
        extensions.put(ext, valueSource);
        final String pr = priority > 0 ? "priority=" + priority : "";
        final String ctr = contracts.length > 0 ? " contracts=\n" + Arrays.stream(contracts)
                .map(aClass -> "\t\t\t\t" + RenderUtils
                        .renderClassLine(aClass)).collect(Collectors.joining("\n")) : "";
        String dop = pr + ctr;
        if (!dop.isEmpty()) {
            dop = "\n\t\t\t" + dop;
        }
        trace("Register", RenderUtils.renderClassLine(ext) + dop, valueSource.getSource());
    }

    /**
     * Record extension (by class or instance) {@link jakarta.ws.rs.client.WebTarget#register(Object, java.util.Map)}.
     *
     * @param value     extension class or instance
     * @param contracts contracts with priority
     */
    public void register(final Object value, final Map<Class<?>, Integer> contracts) {
        final Class<?> ext = value instanceof Class ? (Class<?>) value : value.getClass();
        final SourceAwareValue<Extension> valueSource = value(new Extension(ext, value, contracts));
        extensions.put(ext, valueSource);
        final String ctr = contracts != null ? "\n\t\t\tcontracts=\n" + toStringMap(contracts, "\t\t\t\t") : "";
        trace("Register", RenderUtils.renderClassLine(ext) + ctr, valueSource.getSource());
    }

    /**
     * Record http method {@link jakarta.ws.rs.client.Invocation.Builder#method(String)} and shortcuts like
     * {@link jakarta.ws.rs.client.Invocation.Builder#get()}.
     *
     * @param method http method
     * @param entity entity
     */
    public void method(final String method, final @Nullable Entity<?> entity) {
        method(method, entity, (GenericType<?>) null);
    }

    /**
     * Record http method {@link jakarta.ws.rs.client.Invocation.Builder#method(String)} and shortcuts like
     * {@link jakarta.ws.rs.client.Invocation.Builder#get(Class)}.
     *
     * @param method       http method
     * @param entity       entity
     * @param responseType requested response mapping
     */
    public void method(final String method, final @Nullable Entity<?> entity,
                       final @Nullable Class<?> responseType) {
        method(method, entity, responseType != null ? new GenericType<>(responseType) : null);
    }

    /**
     * Record http method {@link jakarta.ws.rs.client.Invocation.Builder#method(String)} and shortcuts like
     * {@link jakarta.ws.rs.client.Invocation.Builder#get(jakarta.ws.rs.core.GenericType)}.
     *
     * @param method       http method
     * @param entity       entity
     * @param responseType requested response mapping
     */
    public void method(final String method, final @Nullable Entity<?> entity,
                       final @Nullable GenericType<?> responseType) {
        this.method = value(method);
        this.entity = entity != null ? value(entity) : null;
        this.resultMapping = responseType != null ? value(responseType) : null;
        trace("Method", method + (responseType != null ? " type=" + TypeToStringUtils
                .toStringType(responseType.getType(), null) : "")
                + (entity != null ? " entity=" + entity : ""), this.method.getSource());
        // this is the last point where configuration could be recorded
        // NOTE: invocation is not tracked, so in case of .buildGet().invoke(Some.class) class mapping
        // would not be tracked (should not be a problem)
        beforeRequest();
    }

    /**
     * Register required language {@link jakarta.ws.rs.client.Invocation.Builder#acceptLanguage(String...)}.
     *
     * @param value required languages
     */
    public void language(final String... value) {
        languageHeader = value(value);
        trace("Accept Language", Arrays.toString(value), languageHeader.getSource());
    }

    /**
     * Register required encoding {@link jakarta.ws.rs.client.Invocation.Builder#acceptEncoding(String...)}.
     *
     * @param value required encodings
     */
    public void encoding(final String... value) {
        encodingHeader = value(value);
        trace("Accept Encoding", Arrays.toString(value), encodingHeader.getSource());
    }

    /**
     * Register cookie {@link jakarta.ws.rs.client.Invocation.Builder#cookie(String, String)}.
     *
     * @param value cookie
     */
    public void cookie(final Cookie value) {
        final SourceAwareValue<Cookie> valueSource = value(value);
        cookies.put(value.getName(), valueSource);
        trace("Cookie", RuntimeDelegate.getInstance().createHeaderDelegate(Cookie.class).toString(value),
                valueSource.getSource());
    }

    /**
     * Register cache control
     * {@link jakarta.ws.rs.client.Invocation.Builder#cacheControl(jakarta.ws.rs.core.CacheControl)}.
     *
     * @param value cache control
     */
    public void cacheControl(final CacheControl value) {
        cache = value(value);
        trace("Cache", RuntimeDelegate.getInstance()
                .createHeaderDelegate(CacheControl.class).toString(value), cache.getSource());
    }

    /**
     * Register header {@link jakarta.ws.rs.client.Invocation.Builder#header(String, Object)}.
     *
     * @param name  header name
     * @param value header value
     */
    public void header(final String name, final Object value) {
        final SourceAwareValue<Object> valueSource = value(value);
        headers.put(name, valueSource);
        trace("Header", name + "=" + value, valueSource.getSource());
    }

    /**
     * Record multiple headers
     * {@link jakarta.ws.rs.client.Invocation.Builder#headers(jakarta.ws.rs.core.MultivaluedMap)}.
     *
     * @param value headers
     */
    public void headers(final MultivaluedMap<String, Object> value) {
        final String source = getCallSource();
        value.forEach((s, o) -> headers
                .put(s, new SourceAwareValue<>(() -> o.size() == 1 ? o.get(0) : o, source)));
        trace("Headers", toStringMap(value, DBL_TAB), source);
    }

    /**
     * Record target url. Called after {@link jakarta.ws.rs.client.WebTarget#request()}.
     *
     * @param url url
     */
    public void url(final String url) {
        this.url = url;
    }

    // -------------------------------------------------------------- GETTERS


    /**
     * @return query params
     */
    public Map<String, SourceAwareValue<Object>> getQueryParams() {
        return queryParams;
    }

    /**
     * @return matric params
     */
    public Map<String, SourceAwareValue<Object>> getMatrixParams() {
        return matrixParams;
    }

    /**
     * @return path params
     */
    public List<SourceAwareValue<PathParam>> getPathParams() {
        return pathParams;
    }

    /**
     * @return headers
     */
    public Map<String, SourceAwareValue<Object>> getHeaders() {
        return headers;
    }

    /**
     * @return cookies
     */
    public Map<String, SourceAwareValue<Cookie>> getCookies() {
        return cookies;
    }

    /**
     * @return properties
     */
    public Map<String, SourceAwareValue<Object>> getProperties() {
        return properties;
    }

    /**
     * @return extensions
     */
    public Map<Class<?>, SourceAwareValue<Extension>> getExtensions() {
        return extensions;
    }

    /**
     * @return paths
     */
    public List<SourceAwareValue<String>> getPaths() {
        return paths;
    }

    /**
     * @return accept header values
     */
    @Nullable
    public SourceAwareValue<String[]> getAcceptHeader() {
        return acceptHeader;
    }

    /**
     * @return accept language header values
     */
    @Nullable
    public SourceAwareValue<String[]> getLanguageHeader() {
        return languageHeader;
    }

    /**
     * @return accept encoding header values
     */
    @Nullable
    public SourceAwareValue<String[]> getEncodingHeader() {
        return encodingHeader;
    }

    /**
     * @return cache control header value
     */
    @Nullable
    public SourceAwareValue<CacheControl> getCache() {
        return cache;
    }

    /**
     * @return called http method
     */
    @Nullable
    public SourceAwareValue<String> getMethod() {
        return method;
    }

    /**
     * @return used entity
     */
    @Nullable
    public SourceAwareValue<Entity<?>> getEntity() {
        return entity;
    }

    /**
     * Covers both {@link java.lang.Class} and {@link jakarta.ws.rs.core.GenericType} options (in case of class,
     * use generic type constructor).
     *
     * @return result mapping (if specified)
     */
    @Nullable
    public SourceAwareValue<GenericType<?>> getResultMapping() {
        return resultMapping;
    }

    /**
     * @return target url
     */
    @Nullable
    public String getUrl() {
        return url;
    }

    /**
     * @return modifications log
     */
    public String getLog() {
        return log.toString();
    }

    private String getCallSource() {
        return StackUtils.getCallerSource(INFRA);
    }

    @SuppressFBWarnings("VA_FORMAT_STRING_USES_NEWLINE")
    private void trace(final String title, final String value, final String source) {
        log.append(String.format("\t%-40s  %s\n\t\t%s\n\n", title, source == null ? "" : source,
                value.startsWith(DBL_TAB) ? value.substring(2) : value));
    }

    private String toStringMap(final Map<?, ?> map, final String prefix) {
        return map.entrySet().stream().map(entry ->
                        String.format("%s%s=%s", prefix,
                                entry.getKey() instanceof Class
                                        ? RenderUtils.renderClassLine((Class<?>) entry.getKey())
                                        : entry.getKey(),
                                entry.getValue()))
                .collect(Collectors.joining("\n"));
    }

    private <T> SourceAwareValue<T> value(final T supplier) {
        return new SourceAwareValue<>(() -> supplier, getCallSource());
    }

    private void beforeRequest() {
        if (preRequestAction != null) {
            preRequestAction.run();
        }
    }

    /**
     * Path parameter.
     */
    public static class PathParam {
        private final String name;
        private final Object value;
        private final boolean encodeSlashInPath;
        private final boolean encoded;

        /**
         * Create a parameter info object.
         *
         * @param name              param name
         * @param value             param value
         * @param encodeSlashInPath true to encode slashes
         * @param encoded           true if value is already encoded
         */
        public PathParam(final String name, final Object value,
                         final boolean encodeSlashInPath, final boolean encoded) {
            this.name = name;
            this.value = value;
            this.encodeSlashInPath = encodeSlashInPath;
            this.encoded = encoded;
        }

        /**
         * @return param name
         */
        public String getName() {
            return name;
        }

        /**
         * @return param value
         */
        public Object getValue() {
            return value;
        }

        /**
         * @return true if slashes encoding is required
         */
        public boolean isEncodeSlashInPath() {
            return encodeSlashInPath;
        }

        /**
         * @return true if value already encoded
         */
        public boolean isEncoded() {
            return encoded;
        }
    }

    /**
     * Extension info.
     */
    public static class Extension {
        private final Class<?> type;
        private final Object value;
        private final Map<Class<?>, Integer> contracts;

        /**
         * Create extension info.
         *
         * @param type      extension class
         * @param value     extension instance or clas
         * @param priority  priority
         * @param contracts extension contracts
         */
        public Extension(final Class<?> type, final Object value, final int priority, final Class<?>... contracts) {
            this.type = type;
            this.value = value;
            this.contracts = new HashMap<>();
            for (Class<?> contract : contracts) {
                this.contracts.put(contract, priority);
            }
        }

        /**
         * Create extension info.
         *
         * @param type      extension class
         * @param value     extension instance or clas
         * @param contracts extension contracts with priority
         */
        public Extension(final Class<?> type, final Object value, final Map<Class<?>, Integer> contracts) {
            this.type = type;
            this.value = value;
            this.contracts = contracts;
        }

        /**
         * @return extension class
         */
        public Class<?> getType() {
            return type;
        }

        /**
         * @return extension instance or class
         */
        public Object getValue() {
            return value;
        }

        /**
         * @return extension contracts with priority
         */
        public Map<Class<?>, Integer> getContracts() {
            return contracts;
        }
    }
}
