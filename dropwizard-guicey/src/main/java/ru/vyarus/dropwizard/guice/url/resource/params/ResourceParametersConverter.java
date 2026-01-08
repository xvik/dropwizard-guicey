package ru.vyarus.dropwizard.guice.url.resource.params;

import jakarta.ws.rs.ext.ParamConverter;
import jakarta.ws.rs.ext.ParamConverterProvider;
import org.glassfish.jersey.internal.inject.InjectionManager;
import org.glassfish.jersey.internal.inject.ParamConverterFactory;
import org.glassfish.jersey.internal.inject.Providers;
import org.glassfish.jersey.model.Parameter;
import ru.vyarus.dropwizard.guice.url.model.ResourceMethodInfo;
import ru.vyarus.dropwizard.guice.url.model.param.ParameterSource;

/**
 * Converts values, resolved by rest method call analysis using registered {@link jakarta.ws.rs.ext.ParamConverter}.
 * <p>
 * During method analysis, native jersey mechanism is used to recognize {@link org.glassfish.jersey.model.Parameter},
 * which are used here for conversion. But not all available parameters are suitable for conversion: jersey would
 * be able to handle only parameters resolved directly. Guicey also creates "fake" parameters for some form cases
 * (when multiple parameters declared in a single argument).
 * <p>
 * Note that jersey {@link jakarta.ws.rs.ext.ParamConverter} supports two-way conversion: from string and to string.
 * Here to string is used. It is assumed that all registered converters correctly implement both methods
 * (normally only from string is used for request processing).
 *
 * @author Vyacheslav Rusakov
 * @since 21.12.2025
 */
public final class ResourceParametersConverter {

    private ResourceParametersConverter() {
    }

    /**
     * Convert values to string using registered {@link jakarta.ws.rs.ext.ParamConverter}.
     *
     * @param info    call analysis result
     * @param manager jersey injection manager (used to obtain converters)
     */
    public static void convertParameters(final ResourceMethodInfo info, final InjectionManager manager) {
        final ParamConverterFactory factory = new ParamConverterFactory(
                Providers.getProviders(manager, ParamConverterProvider.class),
                Providers.getCustomProviders(manager, ParamConverterProvider.class));
        for (ParameterSource source : info.selectParameterSources(source ->
                // source only means not real declaration (used when single parameter declares multiple values)
                !source.isSourceOnly()
                        // avoid using converter for entity because it would always be to string instead of
                        // json serialization
                        && !Parameter.Source.ENTITY.equals(source.getType()))) {
            final Parameter param = source.getParameter();
            final ParamConverter<?> converter = factory
                    .getConverter(param.getRawType(), param.getType(), param.getAnnotations());
            if (converter != null) {
                convertValue(source, converter, info);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static void convertValue(final ParameterSource source,
                                     final ParamConverter converter,
                                     final ResourceMethodInfo info) {
        final Object value = info.getParameterValue(source);
        final String val = converter.toString(value);
        info.setParameterValue(source, val);
        source.setUsedConverter((Class<ParamConverter<?>>) converter.getClass());
    }
}
