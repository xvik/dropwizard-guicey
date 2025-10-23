package ru.vyarus.dropwizard.guice.test.client.builder.track.impl;

import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import org.jspecify.annotations.Nullable;
import ru.vyarus.dropwizard.guice.test.client.builder.track.RequestData;
import ru.vyarus.dropwizard.guice.test.client.builder.track.RequestTracker;
import ru.vyarus.dropwizard.guice.test.client.builder.track.impl.mock.TargetMock;

import java.net.URI;
import java.util.Arrays;
import java.util.Map;

/**
 * {@link javax.ws.rs.client.WebTarget} object tracker. Used to intercept configuration calls and verify
 * correctness in tests. Does not support direct async and rx apis.
 *
 * @author Vyacheslav Rusakov
 * @since 04.10.2025
 */
@SuppressWarnings({"PMD.TooManyMethods", "PMD.AvoidFieldNameMatchingMethodName"})
public class TargetTracker implements WebTarget, TrackableData {

    private final RequestTracker tracker;
    private final RequestData data;
    private WebTarget target;
    private UriBuilder uriBuilder;

    /**
     * Create an empty web target wrapper (wihtout real target).
     *
     * @param tracker request tracker object
     */
    public TargetTracker(final RequestTracker tracker) {
        this(tracker, null);
    }

    /**
     * Create a web target wrapper.
     *
     * @param tracker request tracker object
     * @param target  real target object
     */
    public TargetTracker(final RequestTracker tracker, @Nullable final WebTarget target) {
        this.tracker = tracker;
        this.data = tracker.getRawData();
        // in case of null just a mock to avoid null
        this.target = target == null ? new TargetMock() : target;
        uriBuilder = new UriBuilderTracker(data, this.target.getUriBuilder());
    }

    @Override
    public URI getUri() {
        return target.getUri();
    }

    @Override
    public UriBuilder getUriBuilder() {
        return uriBuilder;
    }

    @Override
    public WebTarget path(final String path) {
        data.path(path);
        target(target.path(path));
        return this;
    }

    @Override
    public WebTarget resolveTemplate(final String name, final Object value) {
        data.resolveTemplate(name, value, false, false);
        target(target.resolveTemplate(name, value));
        return this;
    }

    @Override
    public WebTarget resolveTemplate(final String name, final Object value, final boolean encodeSlashInPath) {
        data.resolveTemplate(name, value, encodeSlashInPath, false);
        target(target.resolveTemplate(name, value, encodeSlashInPath));
        return this;
    }

    @Override
    public WebTarget resolveTemplateFromEncoded(final String name, final Object value) {
        data.resolveTemplate(name, value, false, true);
        target(target.resolveTemplateFromEncoded(name, value));
        return this;
    }

    @Override
    public WebTarget resolveTemplates(final Map<String, Object> templateValues) {
        data.resolveTemplates(templateValues, false, false);
        target(target.resolveTemplates(templateValues));
        return this;
    }

    @Override
    public WebTarget resolveTemplates(final Map<String, Object> templateValues, final boolean encodeSlashInPath) {
        data.resolveTemplates(templateValues, encodeSlashInPath, false);
        target(target.resolveTemplates(templateValues, encodeSlashInPath));
        return this;
    }

    @Override
    public WebTarget resolveTemplatesFromEncoded(final Map<String, Object> templateValues) {
        data.resolveTemplates(templateValues, false, true);
        target(target.resolveTemplatesFromEncoded(templateValues));
        return this;
    }

    @Override
    public WebTarget matrixParam(final String name, final Object... values) {
        data.matrixParam(name, values);
        target(target.matrixParam(name, values));
        return this;
    }

    @Override
    public WebTarget queryParam(final String name, final Object... values) {
        data.queryParam(name, values);
        target(target.queryParam(name, values));
        return this;
    }

    @Override
    public Invocation.Builder request() {
        data.url(target.getUri().toString());
        return new BuilderTracker(tracker, target.request());
    }

    @Override
    public Invocation.Builder request(final String... acceptedResponseTypes) {
        data.accept(acceptedResponseTypes);
        data.url(target.getUri().toString());
        return new BuilderTracker(tracker, target.request(acceptedResponseTypes));
    }

    @Override
    public Invocation.Builder request(final MediaType... acceptedResponseTypes) {
        data.accept(Arrays.stream(acceptedResponseTypes).map(MediaType::toString).toArray(String[]::new));
        data.url(target.getUri().toString());
        return new BuilderTracker(tracker, target.request(acceptedResponseTypes));
    }

    @Override
    public Configuration getConfiguration() {
        return target.getConfiguration();
    }

    @Override
    public WebTarget property(final String name, final Object value) {
        data.property(name, value);
        target(target.property(name, value));
        return this;
    }

    @Override
    public WebTarget register(final Class<?> componentClass) {
        data.register(componentClass, -1);
        target(target.register(componentClass));
        return this;
    }

    @Override
    public WebTarget register(final Class<?> componentClass, final int priority) {
        data.register(componentClass, priority);
        target(target.register(componentClass, priority));
        return this;
    }

    @Override
    public WebTarget register(final Class<?> componentClass, final Class<?>... contracts) {
        data.register(componentClass, -1, contracts);
        target(target.register(componentClass, contracts));
        return this;
    }

    @Override
    public WebTarget register(final Class<?> componentClass, final Map<Class<?>, Integer> contracts) {
        data.register(componentClass, contracts);
        target(target.register(componentClass, contracts));
        return this;
    }

    @Override
    public WebTarget register(final Object component) {
        data.register(component, -1);
        target(target.register(component));
        return this;
    }

    @Override
    public WebTarget register(final Object component, final int priority) {
        data.register(component, priority);
        target(target.register(component, priority));
        return this;
    }

    @Override
    public WebTarget register(final Object component, final Class<?>... contracts) {
        data.register(component, -1, contracts);
        target(target.register(component, contracts));
        return this;
    }

    @Override
    public WebTarget register(final Object component, final Map<Class<?>, Integer> contracts) {
        data.register(component, contracts);
        target(target.register(component, contracts));
        return this;
    }

    @Override
    public RequestTracker getTracker() {
        return tracker;
    }

    private void target(final WebTarget target) {
        if (target == null) {
            return;
        }
        this.target = target;
    }
}
