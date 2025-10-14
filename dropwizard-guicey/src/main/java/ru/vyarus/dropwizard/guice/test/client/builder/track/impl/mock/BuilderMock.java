package ru.vyarus.dropwizard.guice.test.client.builder.track.impl.mock;

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
import jakarta.ws.rs.core.Response;

import java.util.Locale;

/**
 * {@link jakarta.ws.rs.client.Invocation.Builder} mock object. Used to track configuration correctness in
 * {@link ru.vyarus.dropwizard.guice.test.client.builder.track.RequestTracker} without real target.
 *
 * @author Vyacheslav Rusakov
 * @since 04.10.2025
 */
@SuppressWarnings({"PMD.CouplingBetweenObjects", "PMD.ExcessivePublicCount", "PMD.TooManyMethods"})
public class BuilderMock implements Invocation.Builder {

    @Override
    public Invocation build(final String method) {
        return null;
    }

    @Override
    public Invocation build(final String method, final Entity<?> entity) {
        return null;
    }

    @Override
    public Invocation buildGet() {
        return null;
    }

    @Override
    public Invocation buildDelete() {
        return null;
    }

    @Override
    public Invocation buildPost(final Entity<?> entity) {
        return null;
    }

    @Override
    public Invocation buildPut(final Entity<?> entity) {
        return null;
    }

    @Override
    public AsyncInvoker async() {
        return null;
    }

    @Override
    public Invocation.Builder accept(final String... mediaTypes) {
        return null;
    }

    @Override
    public Invocation.Builder accept(final MediaType... mediaTypes) {
        return null;
    }

    @Override
    public Invocation.Builder acceptLanguage(final Locale... locales) {
        return null;
    }

    @Override
    public Invocation.Builder acceptLanguage(final String... locales) {
        return null;
    }

    @Override
    public Invocation.Builder acceptEncoding(final String... encodings) {
        return null;
    }

    @Override
    public Invocation.Builder cookie(final Cookie cookie) {
        return null;
    }

    @Override
    public Invocation.Builder cookie(final String name, final String value) {
        return null;
    }

    @Override
    public Invocation.Builder cacheControl(final CacheControl cacheControl) {
        return null;
    }

    @Override
    public Invocation.Builder header(final String name, final Object value) {
        return null;
    }

    @Override
    public Invocation.Builder headers(final MultivaluedMap<String, Object> headers) {
        return null;
    }

    @Override
    public Invocation.Builder property(final String name, final Object value) {
        return null;
    }

    @Override
    public CompletionStageRxInvoker rx() {
        return null;
    }

    @Override
    public <T extends RxInvoker> T rx(final Class<T> clazz) {
        return null;
    }

    @Override
    public Response get() {
        return null;
    }

    @Override
    public <T> T get(final Class<T> responseType) {
        return null;
    }

    @Override
    public <T> T get(final GenericType<T> responseType) {
        return null;
    }

    @Override
    public Response put(final Entity<?> entity) {
        return null;
    }

    @Override
    public <T> T put(final Entity<?> entity, final Class<T> responseType) {
        return null;
    }

    @Override
    public <T> T put(final Entity<?> entity, final GenericType<T> responseType) {
        return null;
    }

    @Override
    public Response post(final Entity<?> entity) {
        return null;
    }

    @Override
    public <T> T post(final Entity<?> entity, final Class<T> responseType) {
        return null;
    }

    @Override
    public <T> T post(final Entity<?> entity, final GenericType<T> responseType) {
        return null;
    }

    @Override
    public Response delete() {
        return null;
    }

    @Override
    public <T> T delete(final Class<T> responseType) {
        return null;
    }

    @Override
    public <T> T delete(final GenericType<T> responseType) {
        return null;
    }

    @Override
    public Response head() {
        return null;
    }

    @Override
    public Response options() {
        return null;
    }

    @Override
    public <T> T options(final Class<T> responseType) {
        return null;
    }

    @Override
    public <T> T options(final GenericType<T> responseType) {
        return null;
    }

    @Override
    public Response trace() {
        return null;
    }

    @Override
    public <T> T trace(final Class<T> responseType) {
        return null;
    }

    @Override
    public <T> T trace(final GenericType<T> responseType) {
        return null;
    }

    @Override
    public Response method(final String name) {
        return null;
    }

    @Override
    public <T> T method(final String name, final Class<T> responseType) {
        return null;
    }

    @Override
    public <T> T method(final String name, final GenericType<T> responseType) {
        return null;
    }

    @Override
    public Response method(final String name, final Entity<?> entity) {
        return null;
    }

    @Override
    public <T> T method(final String name, final Entity<?> entity, final Class<T> responseType) {
        return null;
    }

    @Override
    public <T> T method(final String name, final Entity<?> entity, final GenericType<T> responseType) {
        return null;
    }
}
