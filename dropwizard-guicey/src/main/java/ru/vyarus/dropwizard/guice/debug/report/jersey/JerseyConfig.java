package ru.vyarus.dropwizard.guice.debug.report.jersey;

import com.google.common.collect.ImmutableSet;
import org.glassfish.jersey.internal.inject.InjectionResolver;
import org.glassfish.jersey.server.spi.internal.ValueParamProvider;

import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.ext.*;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Configuration for jersey config report {@link JerseyConfigRenderer}.
 * <p>
 * When no items explicitly configured - everything will be showed.
 *
 * @author Vyacheslav Rusakov
 * @since 27.10.2019
 */
public class JerseyConfig {

    private static final Set<Class<?>> EXTENSION_TYPES = ImmutableSet.of(
            ExceptionMapper.class,
            ParamConverterProvider.class,
            ContextResolver.class,
            MessageBodyReader.class,
            MessageBodyWriter.class,
            ReaderInterceptor.class,
            WriterInterceptor.class,
            ContainerRequestFilter.class,
            ContainerResponseFilter.class,
            DynamicFeature.class,
            ValueParamProvider.class,
            InjectionResolver.class
    );

    // remember activation order
    private final Set<Class<?>> types = new LinkedHashSet<>();

    /**
     * Show exception mappers {@link ExceptionMapper}.
     *
     * @return config instance for chained calls
     */
    public JerseyConfig showExceptionMappers() {
        types.add(ExceptionMapper.class);
        return this;
    }

    /**
     * Show message readers {@link MessageBodyReader}.
     *
     * @return config instance for chained calls
     */
    public JerseyConfig showMessageReaders() {
        types.add(MessageBodyReader.class);
        return this;
    }

    /**
     * Show message writers {@link MessageBodyWriter}.
     *
     * @return config instance for chained calls
     */
    public JerseyConfig showMessageWriters() {
        types.add(MessageBodyWriter.class);
        return this;
    }

    /**
     * Show read interceptors {@link ReaderInterceptor}.
     *
     * @return config instance for chained calls
     */
    public JerseyConfig showReadInterceptors() {
        types.add(ReaderInterceptor.class);
        return this;
    }

    /**
     * Show write interceptors {@link WriterInterceptor}.
     *
     * @return config instance for chained calls
     */
    public JerseyConfig showWriteInterceptors() {
        types.add(WriterInterceptor.class);
        return this;
    }

    /**
     * Show request filters {@link ContainerRequestFilter}.
     *
     * @return config instance for chained calls
     */
    public JerseyConfig showRequestFilters() {
        types.add(ContainerRequestFilter.class);
        return this;
    }

    /**
     * Show response filters {@link ContainerResponseFilter}.
     *
     * @return config instance for chained calls
     */
    public JerseyConfig showResponseFilters() {
        types.add(ContainerResponseFilter.class);
        return this;
    }

    /**
     * Show dynamic features {@link DynamicFeature}.
     *
     * @return config instance for chained calls
     */
    public JerseyConfig showDynamicFeatures() {
        types.add(DynamicFeature.class);
        return this;
    }

    /**
     * Show context resolvers {@link ContextResolver}.
     *
     * @return config instance for chained calls
     */
    public JerseyConfig showContextResolvers() {
        types.add(ContextResolver.class);
        return this;
    }

    /**
     * Show param converters {@link ParamConverterProvider}.
     *
     * @return config instance for chained calls
     */
    public JerseyConfig showParamConverters() {
        types.add(ParamConverterProvider.class);
        return this;
    }

    /**
     * Show param value providers {@link ValueParamProvider}.
     *
     * @return config instance for chained calls
     */
    public JerseyConfig showParamValueProviders() {
        types.add(ValueParamProvider.class);
        return this;
    }

    /**
     * Show injection resolvers {@link InjectionResolver}.
     *
     * @return config instance for chained calls
     */
    public JerseyConfig showInjectionResolvers() {
        types.add(InjectionResolver.class);
        return this;
    }

    /**
     * @return types of extensions to show in report or all types if nothing configured
     */
    public Set<Class<?>> getRequiredTypes() {
        return types.isEmpty() ? EXTENSION_TYPES : types;
    }
}
