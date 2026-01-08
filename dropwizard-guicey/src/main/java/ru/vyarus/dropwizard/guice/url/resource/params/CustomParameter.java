package ru.vyarus.dropwizard.guice.url.resource.params;

import org.glassfish.jersey.server.model.Parameter;
import org.jspecify.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * Custom parameter, required to modify automatically created parameters (correct source), because existing
 * implementations constructors are closed from direct usage.
 *
 * @author Vyacheslav Rusakov
 * @since 03.01.2026
 */
public class CustomParameter extends Parameter {

    /**
     * Create new parameter.
     *
     * @param markers      parameter annotations
     * @param marker       marker annotation
     * @param source       parameter type
     * @param sourceName   parameter name
     * @param rawType      raw parameter class
     * @param type         parameter type
     * @param encoded      true for encoded parameter
     * @param defaultValue default value, if provided (with annotation)
     */
    @SuppressWarnings("checkstyle:ParameterNumber")
    public CustomParameter(final Annotation[] markers,
                           final Annotation marker,
                           final Source source,
                           final String sourceName,
                           final Class<?> rawType,
                           final Type type,
                           final boolean encoded,
                           final String defaultValue) {
        super(markers, marker, source, sourceName, rawType, type, encoded, defaultValue);
    }

    /**
     * Correct implementation of parameter source overriding:
     * {@link org.glassfish.jersey.server.model.Parameter#overrideSource(
     * org.glassfish.jersey.server.model.Parameter, org.glassfish.jersey.model.Parameter.Source)}
     * also overrides parameter name, which is wrong.
     *
     * @param parameter original parameter
     * @param source    new source
     * @param name      optionally, new parameter name
     * @return new parameter instance
     */
    public static Parameter overrideSource(final Parameter parameter,
                                           final Source source,
                                           @Nullable final String name) {
        return new CustomParameter(parameter.getAnnotations(),
                parameter.getSourceAnnotation(),
                source,
                name == null ? parameter.getSourceName() : name,
                parameter.getRawType(),
                parameter.getType(),
                parameter.isEncoded(),
                parameter.getDefaultValue());
    }
}
