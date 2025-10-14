package ru.vyarus.dropwizard.guice.test.client.builder.track.impl;

import jakarta.ws.rs.client.AsyncInvoker;
import jakarta.ws.rs.client.CompletionStageRxInvoker;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.RxInvoker;
import jakarta.ws.rs.core.CacheControl;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import ru.vyarus.dropwizard.guice.test.client.builder.track.RequestData;
import ru.vyarus.dropwizard.guice.test.client.builder.track.RequestTracker;

import java.util.Arrays;
import java.util.Locale;

/**
 * {@link jakarta.ws.rs.client.Invocation.Builder} object tracker.
 *
 * @author Vyacheslav Rusakov
 * @since 04.10.2025
 */
@SuppressWarnings({"PMD.CouplingBetweenObjects", "PMD.ExcessivePublicCount", "PMD.TooManyMethods",
        "PMD.AvoidFieldNameMatchingMethodName"})
public class BuilderTracker implements Invocation.Builder, TrackableData {

    private static final String GET = "GET";
    private static final String DELETE = "DELETE";
    private static final String POST = "POST";
    private static final String PUT = "PUT";
    private static final String HEAD = "HEAD";
    private static final String OPTIONS = "OPTIONS";
    private static final String TRACE = "TRACE";

    private final RequestTracker tracker;
    private final RequestData data;
    private final Invocation.Builder target;

    /**
     * Create a request builder tracker.
     *
     * @param tracker tracker object
     * @param builder real builder instance
     */
    public BuilderTracker(final RequestTracker tracker, final Invocation.Builder builder) {
        this.tracker = tracker;
        this.data = tracker.getRawData();
        this.target = builder;
    }

    @Override
    public Invocation build(final String method) {
        data.method(method, null);
        return target.build(method);
    }

    @Override
    public Invocation build(final String method, final Entity<?> entity) {
        data.method(method, entity);
        return target.build(method, entity);
    }

    @Override
    public Invocation buildGet() {
        data.method(GET, null);
        return target.buildGet();
    }

    @Override
    public Invocation buildDelete() {
        data.method(DELETE, null);
        return target.buildDelete();
    }

    @Override
    public Invocation buildPost(final Entity<?> entity) {
        data.method(POST, entity);
        return target.buildPost(entity);
    }

    @Override
    public Invocation buildPut(final Entity<?> entity) {
        data.method(PUT, entity);
        return target.buildPut(entity);
    }

    @Override
    public AsyncInvoker async() {
        // intentionally not tracked
        return target.async();
    }

    @Override
    public Invocation.Builder accept(final String... mediaTypes) {
        data.accept(mediaTypes);
        target.accept(mediaTypes);
        return this;
    }

    @Override
    public Invocation.Builder accept(final MediaType... mediaTypes) {
        data.accept(Arrays.stream(mediaTypes).map(MediaType::toString).toArray(String[]::new));
        target.accept(mediaTypes);
        return this;
    }

    @Override
    public Invocation.Builder acceptLanguage(final Locale... locales) {
        data.language(Arrays.stream(locales).map(Locale::toString).toArray(String[]::new));
        target.acceptLanguage(locales);
        return this;
    }

    @Override
    public Invocation.Builder acceptLanguage(final String... locales) {
        data.language(locales);
        target.acceptLanguage(locales);
        return this;
    }

    @Override
    public Invocation.Builder acceptEncoding(final String... encodings) {
        data.encoding(encodings);
        target.acceptEncoding(encodings);
        return this;
    }

    @Override
    public Invocation.Builder cookie(final Cookie cookie) {
        data.cookie(cookie);
        target.cookie(cookie);
        return this;
    }

    @Override
    public Invocation.Builder cookie(final String name, final String value) {
        cookie(new NewCookie.Builder(name).value(value).build());
        target.cookie(name, value);
        return this;
    }

    @Override
    public Invocation.Builder cacheControl(final CacheControl cacheControl) {
        data.cacheControl(cacheControl);
        target.cacheControl(cacheControl);
        return this;
    }

    @Override
    public Invocation.Builder header(final String name, final Object value) {
        data.header(name, value);
        target.header(name, value);
        return this;
    }

    @Override
    public Invocation.Builder headers(final MultivaluedMap<String, Object> headers) {
        data.headers(headers);
        target.headers(headers);
        return this;
    }

    @Override
    public Invocation.Builder property(final String name, final Object value) {
        data.property(name, value);
        target.property(name, value);
        return this;
    }

    @Override
    public CompletionStageRxInvoker rx() {
        // intentionally not tracked
        return target.rx();
    }

    @Override
    public <T extends RxInvoker> T rx(final Class<T> clazz) {
        // intentionally not tracked
        return target.rx(clazz);
    }

    @Override
    public Response get() {
        data.method(GET, null);
        return target.get();
    }

    @Override
    public <T> T get(final Class<T> responseType) {
        data.method(GET, null, responseType);
        return target.get(responseType);
    }

    @Override
    public <T> T get(final GenericType<T> responseType) {
        data.method(GET, null, responseType);
        return target.get(responseType);
    }

    @Override
    public Response put(final Entity<?> entity) {
        data.method(PUT, entity);
        return target.put(entity);
    }

    @Override
    public <T> T put(final Entity<?> entity, final Class<T> responseType) {
        data.method(PUT, entity, responseType);
        return target.put(entity, responseType);
    }

    @Override
    public <T> T put(final Entity<?> entity, final GenericType<T> responseType) {
        data.method(PUT, entity, responseType);
        return target.put(entity, responseType);
    }

    @Override
    public Response post(final Entity<?> entity) {
        data.method(POST, entity);
        return target.post(entity);
    }

    @Override
    public <T> T post(final Entity<?> entity, final Class<T> responseType) {
        data.method(POST, entity, responseType);
        return target.post(entity, responseType);
    }

    @Override
    public <T> T post(final Entity<?> entity, final GenericType<T> responseType) {
        data.method(POST, entity, responseType);
        return target.post(entity, responseType);
    }

    @Override
    public Response delete() {
        data.method(DELETE, null);
        return target.delete();
    }

    @Override
    public <T> T delete(final Class<T> responseType) {
        data.method(DELETE, null, responseType);
        return target.delete(responseType);
    }

    @Override
    public <T> T delete(final GenericType<T> responseType) {
        data.method(DELETE, null, responseType);
        return target.delete(responseType);
    }

    @Override
    public Response head() {
        data.method(HEAD, null);
        return target.head();
    }

    @Override
    public Response options() {
        data.method(OPTIONS, null);
        return target.options();
    }

    @Override
    public <T> T options(final Class<T> responseType) {
        data.method(OPTIONS, null, responseType);
        return target.options(responseType);
    }

    @Override
    public <T> T options(final GenericType<T> responseType) {
        data.method(OPTIONS, null, responseType);
        return target.options(responseType);
    }

    @Override
    public Response trace() {
        data.method(TRACE, null);
        return target.trace();
    }

    @Override
    public <T> T trace(final Class<T> responseType) {
        data.method(TRACE, null, responseType);
        return target.trace(responseType);
    }

    @Override
    public <T> T trace(final GenericType<T> responseType) {
        data.method(TRACE, null, responseType);
        return target.trace(responseType);
    }

    @Override
    public Response method(final String name) {
        data.method(name, null);
        return target.method(name);
    }

    @Override
    public <T> T method(final String name, final Class<T> responseType) {
        data.method(name, null, responseType);
        return target.method(name, responseType);
    }

    @Override
    public <T> T method(final String name, final GenericType<T> responseType) {
        data.method(name, null, responseType);
        return target.method(name, responseType);
    }

    @Override
    public Response method(final String name, final Entity<?> entity) {
        data.method(name, entity);
        return target.method(name, entity);
    }

    @Override
    public <T> T method(final String name, final Entity<?> entity, final Class<T> responseType) {
        data.method(name, entity, responseType);
        return target.method(name, entity, responseType);
    }

    @Override
    public <T> T method(final String name, final Entity<?> entity, final GenericType<T> responseType) {
        data.method(name, entity, responseType);
        return target.method(name, entity, responseType);
    }

    @Override
    public RequestTracker getTracker() {
        return tracker;
    }
}
