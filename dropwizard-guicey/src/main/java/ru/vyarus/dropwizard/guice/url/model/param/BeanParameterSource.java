package ru.vyarus.dropwizard.guice.url.model.param;

import org.glassfish.jersey.model.Parameter;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Represent bean parameter field. In this case jersey parameter also targets the field. Wrapper object is
 * required to preserve actual field.
 *
 * @author Vyacheslav Rusakov
 * @since 18.12.2025
 */
public class BeanParameterSource extends ParameterSource {

    private final Class<?> beanClass;
    private final Field field;

    /**
     * Create parameter source.
     *
     * @param parameter        jersey parameter
     * @param value            provided value (from argument or bean field)
     * @param resource         resource class
     * @param method           resource method
     * @param argumentPosition argument position (for bean param - bean position)
     * @param beanClass        bean param class
     * @param field            parameter field
     */
    public BeanParameterSource(final Parameter parameter, final Object value, final Class<?> resource,
                               final Method method, final int argumentPosition,
                               final Class<?> beanClass, final Field field) {
        super(parameter, value, resource, method, argumentPosition);
        // class could be resolved from method, but it could be generic so rely on BeanParam type recognition
        this.beanClass = beanClass;
        this.field = field;
    }

    /**
     * @return bean param class
     */
    public Class<?> getBeanClass() {
        return beanClass;
    }

    /**
     * @return target field inside bean param object
     */
    public Field getField() {
        return field;
    }
}
