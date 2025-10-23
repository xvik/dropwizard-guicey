package ru.vyarus.dropwizard.guice.test.client.builder.track.impl.mock;

import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.ext.RuntimeDelegate;

import java.net.URI;
import java.util.Map;

/**
 * {@link javax.ws.rs.client.WebTarget} mock object. Used to track configuration correctness in
 * {@link ru.vyarus.dropwizard.guice.test.client.builder.track.RequestTracker} without real target.
 *
 * @author Vyacheslav Rusakov
 * @since 04.10.2025
 */
@SuppressWarnings("PMD.TooManyMethods")
public class TargetMock implements WebTarget {

    // used to collect uri changes to be able to show the resulted url
    private final UriBuilder uriBuilder = RuntimeDelegate.getInstance().createUriBuilder();

    @Override
    public URI getUri() {
        return uriBuilder.build();
    }

    @Override
    public UriBuilder getUriBuilder() {
        return uriBuilder;
    }

    @Override
    public WebTarget path(final String path) {
        uriBuilder.path(path);
        return this;
    }

    @Override
    public WebTarget resolveTemplate(final String name, final Object value) {
        uriBuilder.resolveTemplate(name, value);
        return null;
    }

    @Override
    public WebTarget resolveTemplate(final String name, final Object value, final boolean encodeSlashInPath) {
        uriBuilder.resolveTemplate(name, value, encodeSlashInPath);
        return null;
    }

    @Override
    public WebTarget resolveTemplateFromEncoded(final String name, final Object value) {
        uriBuilder.resolveTemplateFromEncoded(name, value);
        return null;
    }

    @Override
    public WebTarget resolveTemplates(final Map<String, Object> templateValues) {
        uriBuilder.resolveTemplates(templateValues);
        return null;
    }

    @Override
    public WebTarget resolveTemplates(final Map<String, Object> templateValues, final boolean encodeSlashInPath) {
        uriBuilder.resolveTemplates(templateValues, encodeSlashInPath);
        return null;
    }

    @Override
    public WebTarget resolveTemplatesFromEncoded(final Map<String, Object> templateValues) {
        uriBuilder.resolveTemplatesFromEncoded(templateValues);
        return null;
    }

    @Override
    public WebTarget matrixParam(final String name, final Object... values) {
        return null;
    }

    @Override
    public WebTarget queryParam(final String name, final Object... values) {
        return null;
    }

    @Override
    public Invocation.Builder request() {
        return new BuilderMock();
    }

    @Override
    public Invocation.Builder request(final String... acceptedResponseTypes) {
        return new BuilderMock();
    }

    @Override
    public Invocation.Builder request(final MediaType... acceptedResponseTypes) {
        return new BuilderMock();
    }

    @Override
    public Configuration getConfiguration() {
        return null;
    }

    @Override
    public WebTarget property(final String name, final Object value) {
        return null;
    }

    @Override
    public WebTarget register(final Class<?> componentClass) {
        return null;
    }

    @Override
    public WebTarget register(final Class<?> componentClass, final int priority) {
        return null;
    }

    @Override
    public WebTarget register(final Class<?> componentClass, final Class<?>... contracts) {
        return null;
    }

    @Override
    public WebTarget register(final Class<?> componentClass, final Map<Class<?>, Integer> contracts) {
        return null;
    }

    @Override
    public WebTarget register(final Object component) {
        return null;
    }

    @Override
    public WebTarget register(final Object component, final int priority) {
        return null;
    }

    @Override
    public WebTarget register(final Object component, final Class<?>... contracts) {
        return null;
    }

    @Override
    public WebTarget register(final Object component, final Map<Class<?>, Integer> contracts) {
        return null;
    }
}
