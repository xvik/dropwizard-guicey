package ru.vyarus.dropwizard.guice.url.model.param;

import org.glassfish.jersey.model.Parameter;

import java.lang.reflect.Method;

/**
 * Special case: argument declares multipart parameter or multimap used for values aggregation.
 * In this case it would be impossible to use a jersey converters mechanism, so only stored parameter location
 * info (to be able to show in the debug report).
 * <p>
 * Jersey parameter would miss the name because it was not declared in annotation: jersey parameter represent
 * the root object itself, but {@link ru.vyarus.dropwizard.guice.url.model.param.DeclarationSource} represents
 * each individual parameter (in map or generic multipart object).
 *
 * @author Vyacheslav Rusakov
 * @since 19.12.2025
 */
public class DeclarationSource extends ParameterSource {

    private final String name;

    /**
     * Create parameter source.
     *
     * @param parameter        jersey parameter (will miss actual name because parameter represent the root object)
     * @param value            provided value (from argument or bean field)
     * @param resource         resource class
     * @param method           resource method
     * @param argumentPosition argument position (for bean param - bean position)
     * @param name             parameter name
     */
    public DeclarationSource(final Parameter parameter, final Object value,
                             final Class<?> resource, final Method method, final int argumentPosition,
                             final String name) {
        super(parameter, value, resource, method, argumentPosition);
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }
}
