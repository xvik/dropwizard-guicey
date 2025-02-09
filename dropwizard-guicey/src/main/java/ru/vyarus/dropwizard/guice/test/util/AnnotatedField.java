package ru.vyarus.dropwizard.guice.test.util;

import org.junit.jupiter.api.extension.TestInstances;
import org.junit.platform.commons.util.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

/**
 * Annotated field wrapper. Used to simplify work with test fields by hiding all required reflection.
 *
 * @param <A> annotation type
 * @param <T> value type
 * @author Vyacheslav Rusakov
 * @since 07.02.2025
 */
@SuppressWarnings("PMD.GodClass")
public class AnnotatedField<A extends Annotation, T> {
    private final A annotation;
    private final Field field;
    private final Class<?> testClass;
    // custom data assigned during processing
    private Map<String, Object> data;

    public AnnotatedField(final A annotation,
                          final Field field,
                          final Class<?> testClass) {
        this.annotation = annotation;
        this.field = ReflectionUtils.makeAccessible(field);
        this.testClass = testClass;
    }

    /**
     * @return field annotation instance (type of annotation defined by initial fields search)
     */
    public A getAnnotation() {
        return annotation;
    }

    /**
     * @return field class type
     */
    @SuppressWarnings("unchecked")
    public Class<T> getType() {
        return (Class<T>) field.getType();
    }

    /**
     * @return class that declares field
     */
    public Class<?> getDeclaringClass() {
        return field.getDeclaringClass();
    }

    /**
     * @return field name
     */
    public String getName() {
        return field.getName();
    }

    /**
     * @return field instance
     */
    public Field getField() {
        return field;
    }

    /**
     * @param instance test instance (or null for static field)
     * @return test field value
     * @throws java.lang.IllegalStateException if non-static field resolved with null instance or other error appear
     */
    @SuppressWarnings("unchecked")
    public T getValue(final Object instance) {
        if (instance == null && !isStatic()) {
            throw new IllegalStateException("Field " + toStringField()
                    + " is not static: test instance required for obtaining value");
        }
        if (!isCompatible(instance)) {
            throw new IllegalStateException("Invalid instance provided: "
                    + (instance == null ? null : instance.getClass())
                    + " for field " + toStringField());
        }
        try {
            return (T) field.get(instance);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Failed to get field " + toStringField()
                    + " value", e);
        }
    }

    /**
     * In case of nested test, there would be root class instance and nested instance. It is important to select
     * the correct instance for field manipulation: the correct test instance would be selected by preserved test class.
     *
     * @param instances test instances
     * @return field value
     */
    public T getValue(final TestInstances instances) {
        return getValue(findRequiredInstance(instances));
    }

    /**
     * @param instance test instance (or null for static field)
     * @param value    field value
     * @throws java.lang.IllegalStateException if non-static field set with null instance or other error appear
     */
    public void setValue(final Object instance, final T value) {
        if (instance == null && !isStatic()) {
            throw new IllegalStateException("Field " + toStringField()
                    + " is not static: test instance required for setting value");
        }
        if (!isCompatible(instance)) {
            throw new IllegalStateException("Invalid instance provided: "
                    + (instance == null ? null : instance.getClass())
                    + " for field " + toStringField());
        }
        try {
            field.set(instance, value);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Failed to set field " + toStringField() + " value to " + value, e);
        }
    }

    /**
     * In case of nested test, there would be root class instance and nested instance. It is important to select
     * the correct instance for field manipulation: the correct test instance would be selected by preserved test class.
     *
     * @param instances test instances
     * @param value     value to set
     */
    public void setValue(final TestInstances instances, final T value) {
        setValue(findRequiredInstance(instances), value);
    }

    /**
     * @return true if field is static
     */
    public boolean isStatic() {
        return Modifier.isStatic(field.getModifiers());
    }

    /**
     * @return true if field declared directly in test class (not in base class)
     */
    public boolean isTestOwnField() {
        return field.getDeclaringClass().equals(testClass);
    }

    /**
     * Validation option: throw error if field is not static.
     */
    public void requireStatic() {
        if (!isStatic()) {
            throw new IllegalStateException(String.format(
                    "Field %s annotated with @%s, must be static",
                    toStringField(), annotation.annotationType().getSimpleName()
            ));
        }
    }

    /**
     * Validation option: throw error if field is static.
     */
    public void requireNonStatic() {
        if (isStatic()) {
            throw new IllegalStateException(String.format(
                    "Field %s annotated with @%s, must not be static",
                    toStringField(), annotation.annotationType().getSimpleName()
            ));
        }
    }

    /**
     * Required to prevent incorrect usage (field resolution with a wrong instance).
     *
     * @param instance instance to check
     * @return true if the provided instance is a field class instance, false otherwise
     */
    public boolean isCompatible(final Object instance) {
        return isStatic() || (instance != null && testClass.isAssignableFrom(instance.getClass()));
    }

    /**
     * In case of nested tests, test instances would contain multiple test instances. It is important to
     * select the correct one (using preserved original test class).
     *
     * @param instances test instances
     * @return test instance or null
     * @throws java.lang.IllegalStateException if a test instances object provided but does not contain the
     *                                         required test instance
     */
    public Object findRequiredInstance(final TestInstances instances) {
        if (instances == null) {
            return null;
        }
        return instances.findInstance(testClass).orElseThrow(() ->
                new IllegalStateException("No test instance found for test class: " + testClass));
    }

    /**
     * Apply custom value for the field object. Useful during field processing to mark is as processed or assign
     * an additional state.
     *
     * @param key   key
     * @param value value
     */
    public void setCustomData(final String key, final Object value) {
        if (data == null) {
            data = new HashMap<>();
        }
        data.put(key, value);
    }

    /**
     * Get custom value.
     *
     * @param key key
     * @param <K> value type
     * @return value or null
     */
    @SuppressWarnings("unchecked")
    public <K> K getCustomData(final String key) {
        return data == null ? null : (K) data.get(key);
    }

    /**
     * Note: if key set with null value - it would be considered as false.
     *
     * @param key key
     * @return true if non null custom value set
     */
    public boolean isCustomDataSet(final String key) {
        return data != null && data.get(key) != null;
    }

    /**
     * Clear custom state.
     */
    public void clearCustomData() {
        if (data != null) {
            data.clear();
        }
    }

    /**
     * @return to string field with class and reduced package
     */
    public String toStringField() {
        return TestFieldUtils.toString(field);
    }

    @Override
    public String toString() {
        return toStringField() + " ("
                + "@" + annotation.annotationType().getSimpleName() + (isStatic() ? " static" : "")
                + " " + field.getType().getSimpleName()
                + ")";
    }
}
