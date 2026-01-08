package ru.vyarus.dropwizard.guice.url.model.param;

import jakarta.ws.rs.ext.ParamConverter;
import org.glassfish.jersey.model.Parameter;

import java.lang.reflect.Method;

/**
 * Wrapper for jersey {@link org.glassfish.jersey.model.Parameter} with additional information about declaration
 * context. Additional information is required for reporting and filtering (e.g. in case of sub-resource calls
 * parameters could be mixed from sub resource lookup method and resource method call).
 *
 * @author Vyacheslav Rusakov
 * @since 18.12.2025
 */
public class ParameterSource {

    private final Parameter parameter;
    private final Object value;
    private final Class<?> resource;
    private final Method method;
    private final int argumentPosition;
    private Class<ParamConverter<?>> usedConverter;

    /**
     * Create parameter source.
     *
     * @param parameter        jersey parameter
     * @param value            provided value (from argument or bean field)
     * @param resource         resource class
     * @param method           resource method
     * @param argumentPosition argument position (for bean param - bean position)
     */
    public ParameterSource(final Parameter parameter, final Object value,
                           final Class<?> resource, final Method method, final int argumentPosition) {
        this.parameter = parameter;
        this.value = value;
        this.resource = resource;
        this.method = method;
        this.argumentPosition = argumentPosition;
    }

    /**
     * @return jersey parameter instance (could be used for conversions)
     */
    public Parameter getParameter() {
        return parameter;
    }

    /**
     * @return user provided value (argument or bean param field value)
     */
    public Object getValue() {
        return value;
    }

    /**
     * @return resource class
     */
    public Class<?> getResource() {
        return resource;
    }

    /**
     * @return source method
     */
    public Method getMethod() {
        return method;
    }

    /**
     * @return method argument number
     */
    public int getArgumentPosition() {
        return argumentPosition;
    }

    /**
     * @return applied converter or null
     */
    public Class<ParamConverter<?>> getUsedConverter() {
        return usedConverter;
    }

    /**
     * @param usedConverter register used converter
     */
    public void setUsedConverter(final Class<ParamConverter<?>> usedConverter) {
        this.usedConverter = usedConverter;
    }

    /**
     * @return parameter type
     */
    public Parameter.Source getType() {
        return parameter.getSource();
    }

    /**
     * @return parameter name or null for entity
     */
    public String getName() {
        return parameter.getSourceName();
    }

    /**
     * @return true if it's a bean parameter source
     */
    public boolean isBeanParam() {
        return this instanceof BeanParameterSource;
    }

    /**
     * Used for multipart params and urlencoded forms multimap which must be handled manually. Such sources
     * could only be used for debug reports (not for conversion).
     *
     * @return true if only declaration source is contained
     */
    public boolean isSourceOnly() {
        return this instanceof DeclarationSource;
    }

    @Override
    @SuppressWarnings("PMD.UseStringBufferForStringAppends")
    public String toString() {
        String name = parameter.getSourceName();
        // for source-only cases annotation can't keep the name (in this case argument contains multiple params)
        if (isSourceOnly()) {
            name = "\"" + name + "\" from";
        } else if (parameter.getSourceAnnotation() != null) {
            name = "@" + parameter.getSourceAnnotation().annotationType().getSimpleName() + "(\"" + name + "\")";
        }
        return parameter.getSource() + " " + name + " " + parameter.getRawType().getSimpleName();
    }
}
