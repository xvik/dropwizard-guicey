package ru.vyarus.dropwizard.guice.test.client.builder.track.impl;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriBuilderException;
import ru.vyarus.dropwizard.guice.module.installer.util.PathUtils;
import ru.vyarus.dropwizard.guice.test.client.builder.track.RequestData;
import ru.vyarus.dropwizard.guice.url.resource.ResourceAnalyzer;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.Map;

/**
 * {@link javax.ws.rs.core.UriBuilder} tracker for
 * {@link ru.vyarus.dropwizard.guice.test.client.builder.track.RequestTracker}.
 * <p>
 * Path modification like {@link #scheme(String)} or {@link #replacePath(String)} or
 * {@link #replaceQueryParam(String, Object...)} are tracked as simple path or parameter assignment.
 * Total accuracy is not important: it's important to DETECT all changes and show the calling source for them.
 *
 * @author Vyacheslav Rusakov
 * @since 06.10.2025
 */
@SuppressWarnings("PMD.TooManyMethods")
public class UriBuilderTracker extends UriBuilder {

    private final RequestData data;
    private final UriBuilder builder;

    /**
     * Create builder tracker.
     *
     * @param data data collector
     * @param builder real builder
     */
    public UriBuilderTracker(final RequestData data, final UriBuilder builder) {
        this.data = data;
        this.builder = builder;
    }

    @SuppressWarnings({"checkstyle:NoClone", "PMD.CloneMethodMustImplementCloneable"})
    @Override
    public UriBuilderTracker clone() {
        throw new UnsupportedOperationException();
    }

    @Override
    public UriBuilder uri(final URI uri) {
        data.path(uri.toString());
        builder.uri(uri);
        return this;
    }

    @Override
    public UriBuilder uri(final String uriTemplate) {
        data.path(uriTemplate);
        builder.uri(uriTemplate);
        return this;
    }

    @Override
    public UriBuilder scheme(final String scheme) {
        // not correct by does not matter
        data.path(scheme);
        builder.scheme(scheme);
        return this;
    }

    @Override
    public UriBuilder schemeSpecificPart(final String ssp) {
        data.path(ssp);
        builder.schemeSpecificPart(ssp);
        return this;
    }

    @Override
    public UriBuilder userInfo(final String ui) {
        data.path(ui);
        builder.userInfo(ui);
        return this;
    }

    @Override
    public UriBuilder host(final String host) {
        data.path(host);
        builder.host(host);
        return this;
    }

    @Override
    public UriBuilder port(final int port) {
        data.path(String.valueOf(port));
        builder.port(port);
        return this;
    }

    @Override
    public UriBuilder replacePath(final String path) {
        data.path(path);
        builder.replacePath(path);
        return this;
    }

    @Override
    public UriBuilder path(final String path) {
        data.path(path);
        builder.path(path);
        return this;
    }

    @Override
    public UriBuilder path(final Class resource) {
        data.path(ResourceAnalyzer.getResourcePath(resource));
        builder.path(resource.getName());
        return this;
    }

    @Override
    public UriBuilder path(final Class resource, final String method) {
        data.path(ResourceAnalyzer.getMethodPath(resource, method));
        builder.path(resource.getName() + "#" + method);
        return this;
    }

    @Override
    public UriBuilder path(final Method method) {
        data.path(ResourceAnalyzer.getMethodPath(method));
        builder.path(method);
        return this;
    }

    @Override
    public UriBuilder segment(final String... segments) {
        data.path(PathUtils.path(segments));
        builder.segment(segments);
        return this;
    }

    @Override
    public UriBuilder replaceMatrix(final String matrix) {
        if (matrix != null) {
            final Multimap<String, String> vals = parseParams(matrix, ";");
            vals.asMap().forEach((s, strings) -> data
                    .matrixParam(s, strings.size() == 1 ? new Object[]{strings.iterator().next()} : strings.toArray()));
        }
        builder.replaceMatrix(matrix);
        return this;
    }

    @Override
    public UriBuilder matrixParam(final String name, final Object... values) {
        data.matrixParam(name, values);
        builder.matrixParam(name, values);
        return this;
    }

    @Override
    public UriBuilder replaceMatrixParam(final String name, final Object... values) {
        data.matrixParam(name, values);
        builder.replaceMatrixParam(name, values);
        return this;
    }

    @Override
    public UriBuilder replaceQuery(final String query) {
        if (query != null) {
            final Multimap<String, String> vals = parseParams(query, "&");
            vals.asMap().forEach((name, values) -> data
                    .queryParam(name, values.size() == 1 ? new Object[]{values.iterator().next()} : values.toArray()));
        }
        builder.replaceQuery(query);
        return this;
    }

    @Override
    public UriBuilder queryParam(final String name, final Object... values) {
        data.queryParam(name, values);
        builder.queryParam(name, values);
        return this;
    }

    @Override
    public UriBuilder replaceQueryParam(final String name, final Object... values) {
        data.queryParam(name, values);
        builder.replaceQueryParam(name, values);
        return this;
    }

    @Override
    public UriBuilder fragment(final String fragment) {
        data.path(fragment);
        builder.fragment(fragment);
        return this;
    }

    @Override
    public UriBuilder resolveTemplate(final String name, final Object value) {
        data.resolveTemplate(name, value, false, false);
        builder.resolveTemplate(name, value);
        return this;
    }

    @Override
    public UriBuilder resolveTemplate(final String name, final Object value, final boolean encodeSlashInPath) {
        data.resolveTemplate(name, value, encodeSlashInPath, false);
        builder.resolveTemplate(name, value, encodeSlashInPath);
        return this;
    }

    @Override
    public UriBuilder resolveTemplateFromEncoded(final String name, final Object value) {
        data.resolveTemplate(name, value, false, true);
        builder.resolveTemplateFromEncoded(name, value);
        return this;
    }

    @Override
    public UriBuilder resolveTemplates(final Map<String, Object> templateValues) {
        data.resolveTemplates(templateValues, false, false);
        builder.resolveTemplates(templateValues);
        return this;
    }

    @Override
    public UriBuilder resolveTemplates(final Map<String, Object> templateValues,
                                       final boolean encodeSlashInPath) throws IllegalArgumentException {
        data.resolveTemplates(templateValues, encodeSlashInPath, false);
        builder.resolveTemplates(templateValues, encodeSlashInPath);
        return this;
    }

    @Override
    public UriBuilder resolveTemplatesFromEncoded(final Map<String, Object> templateValues) {
        data.resolveTemplates(templateValues, false, true);
        builder.resolveTemplatesFromEncoded(templateValues);
        return this;
    }

    // ---------------------------------------------------------------------- NOT TRACKED

    @Override
    public URI buildFromMap(final Map<String, ?> values) {
        return builder.buildFromMap(values);
    }

    @Override
    public URI buildFromMap(final Map<String, ?> values, final boolean encodeSlashInPath)
            throws IllegalArgumentException, UriBuilderException {
        return builder.buildFromMap(values, encodeSlashInPath);
    }

    @Override
    public URI buildFromEncodedMap(final Map<String, ?> values) throws IllegalArgumentException, UriBuilderException {
        return builder.buildFromEncoded(values);
    }

    @Override
    public URI build(final Object... values) throws IllegalArgumentException, UriBuilderException {
        return builder.build(values);
    }

    @Override
    public URI build(final Object[] values, final boolean encodeSlashInPath)
            throws IllegalArgumentException, UriBuilderException {
        return builder.build(values, encodeSlashInPath);
    }

    @Override
    public URI buildFromEncoded(final Object... values) throws IllegalArgumentException, UriBuilderException {
        return builder.buildFromEncoded(values);
    }

    @Override
    public String toTemplate() {
        return builder.toTemplate();
    }

    private Multimap<String, String> parseParams(final String params, final String separator) {
        final String[] pars = params.split(separator);
        final Multimap<String, String> vals = HashMultimap.create();
        for (String param : pars) {
            final String[] parts = param.split("=");
            if (parts.length == 2) {
                vals.put(parts[0], parts[1]);
            } else {
                vals.put(parts[0], null);
            }
        }
        return vals;
    }
}
