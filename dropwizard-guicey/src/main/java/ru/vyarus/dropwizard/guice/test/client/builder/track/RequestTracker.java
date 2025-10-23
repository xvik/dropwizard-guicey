package ru.vyarus.dropwizard.guice.test.client.builder.track;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.ext.RuntimeDelegate;
import org.jspecify.annotations.Nullable;
import ru.vyarus.dropwizard.guice.test.client.builder.track.impl.TargetTracker;
import ru.vyarus.dropwizard.guice.test.client.builder.track.impl.TrackableData;
import ru.vyarus.dropwizard.guice.test.client.util.SourceAwareValue;
import ru.vyarus.java.generics.resolver.util.TypeToStringUtils;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Tracks jersey client api configuration. Could be used to record all applied changes for
 * {@link javax.ws.rs.client.WebTarget} and {@link javax.ws.rs.client.Invocation.Builder}.
 * <p>
 * Limitations: rx and async apis are not tracked. Also, invocation calls not handled, so for example, in case of
 * {@code buildGet().invoke(Some.class)}, requested mapping class would not be recorded because
 * {@link javax.ws.rs.client.Invocation} object is not tracked.
 * <p>
 * For example, to track real request configuration:
 * <pre><code>
 *     RequestTracker tracker = new RequestTracker();
 *     WebTarget target = tracker.track(originalTarget);
 *     target.path("..").request().get();
 *     // print changes to console
 *     System.out.println(tracker.getLog());
 * </code></pre>
 * <p>
 * {@link #track()} could be used to record modifications for a target mock. This could be used to test
 * request configuration api (when only applied changes must be verified).
 * <p>
 * {@link Runnable} could be used to execute something just before request execution (when all data collected):
 * <pre><code>
 *      RequestTracker tracker = new RequestTracker()'
 *      WebTarget target = tracker.track(originalTarget, ()-> System.out.println(tracker.getLog()));
 * </code></pre>
 * <p>
 * Tracker could be resolved from tarcked {@link javax.ws.rs.client.WebTarget} and
 * {@link javax.ws.rs.client.Invocation.Builder} objects with
 * {@link #lookupTracker(javax.ws.rs.client.WebTarget)} and
 * {@link #lookupTracker(javax.ws.rs.client.Invocation.Builder)}.
 *
 * @author Vyacheslav Rusakov
 * @since 04.10.2025
 */
public class RequestTracker {

    private RequestData data;

    /**
     * Lookup tracker in request builder object.
     *
     * @param object request object
     * @return tracker or null if the object doesn't support tracking
     */
    public static Optional<RequestTracker> lookupTracker(final WebTarget object) {
        return getTracker(object);
    }

    /**
     * Lookup tracker in request builder object.
     *
     * @param object request object
     * @return tracker or null if the object doesn't support tracking
     */
    public static Optional<RequestTracker> lookupTracker(final Invocation.Builder object) {
        return getTracker(object);
    }

    /**
     * Record request changes without actual request execution. Useful for testing apis, modifying request.
     * Use mocker {@link javax.ws.rs.client.WebTarget} inside
     * ({@link ru.vyarus.dropwizard.guice.test.client.builder.track.impl.mock.TargetMock}) which does not implement
     * request processing methods (just accept configuration).
     *
     * @return tracked target
     */
    public WebTarget track() {
        return track((Runnable) null);
    }

    /**
     * Record request changes without actual request execution. Useful for testing apis, modifying request.
     * Use mocker {@link javax.ws.rs.client.WebTarget} inside
     * ({@link ru.vyarus.dropwizard.guice.test.client.builder.track.impl.mock.TargetMock}) which does not implement
     * request processing methods (just accept configuration).
     *
     * @param preRequestAction action to execute before request execution (could be used to log changes)
     * @return tracked target
     */
    public WebTarget track(final @Nullable Runnable preRequestAction) {
        data = new RequestData(preRequestAction);
        return new TargetTracker(this);
    }

    /**
     * Track real request. Useful to track request configuration (with sources).
     * <p>
     * IMPORTANT: tracked target object can't be re-used (it does not create new target instances for each call
     * (whereas original target is correctly re-created).
     *
     * @param target original web target
     * @return tracked target
     */
    public WebTarget track(final WebTarget target) {
        return track(target, null);
    }

    /**
     * Track real request. Useful to track request configuration (with sources).
     * <p>
     * IMPORTANT: tracked target object can't be re-used (it does not create new target instances for each call
     * (whereas original target is correctly re-created).
     *
     * @param target           original web target
     * @param preRequestAction action to execute before request execution (could be used to log changes)
     * @return tracked target
     */
    public WebTarget track(final WebTarget target, final @Nullable Runnable preRequestAction) {
        data = new RequestData(preRequestAction);
        return new TargetTracker(this, target);
    }

    // ------------------------------------------------------------------------ TRACKED DATA

    /**
     * {@link javax.ws.rs.client.WebTarget#queryParam(String, Object...)}.
     * <p>
     * Note: returned value may contain array if multiple values configured.
     *
     * @return applied query params
     */
    public Map<String, Object> getQueryParams() {
        // map may contain nulls
        final Map<String, Object> res = new HashMap<>();
        data.getQueryParams().forEach((s, val) -> res.put(s, val.get()));
        return res;
    }

    /**
     * {@link javax.ws.rs.client.WebTarget#matrixParam(String, Object...)}.
     * <p>
     * Note: returned value may contain array if multiple values configured.
     *
     * @return applied matric params
     */
    public Map<String, Object> getMatrixParams() {
        // map may contain nulls
        final Map<String, Object> res = new HashMap<>();
        data.getMatrixParams().forEach((s, val) -> res.put(s, val.get()));
        return res;
    }

    /**
     * {@link javax.ws.rs.client.WebTarget#resolveTemplate(String, Object)} (and all other variations).
     * <p>
     * See {@link RequestData#getPathParams()} for encoding info.
     *
     * @return applied path params
     */
    public Map<String, Object> getPathParams() {
        return data.getPathParams().stream()
                .collect(Collectors.toMap(o -> o.get().getName(),
                        o -> o.get().getValue()));
    }

    /**
     * {@link javax.ws.rs.client.Invocation.Builder#header(String, Object)}.
     *
     * @return applied headers
     */
    public Map<String, Object> getHeaders() {
        return data.getHeaders().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().get()));
    }

    /**
     * {@link javax.ws.rs.client.Invocation.Builder#cookie(String, String)}.
     *
     * @return applied cookies
     */
    public Map<String, Cookie> getCookies() {
        return data.getCookies().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().get()));
    }

    /**
     * {@link javax.ws.rs.client.WebTarget#property(String, Object)},
     * {@link javax.ws.rs.client.Invocation.Builder#property(String, Object)}.
     *
     * @return applied properties
     */
    public Map<String, Object> getProperties() {
        return data.getProperties().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().get()));
    }

    /**
     * {@link javax.ws.rs.client.WebTarget#register(Class)}.
     * <p>
     * Values in returned map could be a class or instance (if instance was used for configuration).
     * <p>
     * See {@link RequestData#getExtensions()} for applied contracts.
     *
     * @return applied extensions
     */
    public Map<Class<?>, Object> getExtensions() {
        return data.getExtensions().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        e -> e.getValue().get().getValue()));
    }

    /**
     * {@link javax.ws.rs.client.WebTarget#path(String)}.
     * <p>
     * NOTE: does not count {@link javax.ws.rs.client.WebTarget#getUriBuilder()} changes.
     *
     * @return applied paths
     */
    public List<String> getPaths() {
        return data.getPaths().stream().map(SourceAwareValue::get).collect(Collectors.toList());
    }

    /**
     * {@link javax.ws.rs.client.WebTarget#request(String...)},
     * {@link javax.ws.rs.client.Invocation.Builder#accept(String...)}.
     *
     * @return applied expected response types
     */
    public List<String> getAcceptHeader() {
        return data.getAcceptHeader() == null ? Collections.emptyList()
                : Arrays.asList(data.getAcceptHeader().get());
    }

    /**
     * {@link javax.ws.rs.client.Invocation.Builder#acceptLanguage(String...)}.
     *
     * @return applied expected languages
     */
    public List<String> getLanguageHeader() {
        return data.getLanguageHeader() == null ? Collections.emptyList()
                : Arrays.asList(data.getLanguageHeader().get());
    }

    /**
     * {@link javax.ws.rs.client.Invocation.Builder#acceptEncoding(String...)}.
     *
     * @return applied expected encodings
     */
    public List<String> getEncodingHeader() {
        return data.getEncodingHeader() == null ? Collections.emptyList()
                : Arrays.asList(data.getEncodingHeader().get());
    }

    /**
     * {@link javax.ws.rs.client.Invocation.Builder#cacheControl(javax.ws.rs.core.CacheControl)}.
     *
     * @return applied cache control object (cache header)
     * @see #getCacheHeader()
     */
    public CacheControl getCache() {
        return data.getCache() == null ? null : data.getCache().get();
    }

    /**
     * {@link javax.ws.rs.client.Invocation.Builder#cacheControl(javax.ws.rs.core.CacheControl)}.
     *
     * @return applied cache header
     * @see #getCache()
     */
    public String getCacheHeader() {
        final CacheControl cc = getCache();
        return cc == null ? null
                : RuntimeDelegate.getInstance().createHeaderDelegate(CacheControl.class).toString(cc);
    }

    /**
     * {@link javax.ws.rs.client.Invocation.Builder#method(String)} and shortcuts like
     * {@link javax.ws.rs.client.Invocation.Builder#get()}.
     *
     * @return request HTTP method or null (if request method wasn't configred)
     */
    @Nullable
    public String getHttpMethod() {
        return data.getMethod() == null ? null : data.getMethod().get();
    }

    /**
     * {@link javax.ws.rs.client.Invocation.Builder#method(String, javax.ws.rs.client.Entity)} and shortcuts like
     * {@link javax.ws.rs.client.Invocation.Builder#post(javax.ws.rs.client.Entity)}.
     *
     * @return request entity or null
     */
    @Nullable
    public Entity<?> getEntity() {
        return data.getEntity() == null ? null : data.getEntity().get();
    }

    /**
     * Result mapping from apis like {@link javax.ws.rs.client.Invocation.Builder#get(Class)}.
     *
     * @return result mapping type or null
     */
    @Nullable
    public Type getResultMapping() {
        return data.getResultMapping() == null ? null : data.getResultMapping().get().getType();
    }

    /**
     * Result mapping from apis like {@link javax.ws.rs.client.Invocation.Builder#get(Class)}.
     *
     * @return result class (for {@link javax.ws.rs.core.GenericType} it would be just a root class without generics)
     */
    @Nullable
    public Class<?> getResultMappingClass() {
        return data.getResultMapping() == null ? null : data.getResultMapping().get().getRawType();
    }

    /**
     * Result mapping from apis like {@link javax.ws.rs.client.Invocation.Builder#get(Class)}.
     *
     * @return string representation of resulted type, including generics (like "List&lt;Something&gt;")
     */
    @Nullable
    public String getResultMappingString() {
        return data.getResultMapping() == null ? null : TypeToStringUtils
                .toStringType(data.getResultMapping().get().getType(), null);
    }

    /**
     * {@link javax.ws.rs.client.WebTarget#getUri()}.
     * <p>
     * Url is recorded only after invocation builder creation with {@link javax.ws.rs.client.WebTarget#request()}.
     *
     * @return the resulted url or null
     */
    @Nullable
    public String getUrl() {
        return data.getUrl();
    }

    /**
     * Log shows what changed, where and at what order.
     *
     * @return request modifications log (with source references)
     */
    public String getLog() {
        final String log = data.getLog();
        return "\n" + (log.isEmpty() ? "\tNo configurations" : log);
    }

    /**
     * Could be used when modification source data is also required (shortcuts above remove modification source info).
     *
     * @return raw collected data object
     */
    public RequestData getRawData() {
        return data;
    }

    private static Optional<RequestTracker> getTracker(final Object object) {
        return Optional.ofNullable(object instanceof TrackableData ? ((TrackableData) object).getTracker() : null);
    }
}
