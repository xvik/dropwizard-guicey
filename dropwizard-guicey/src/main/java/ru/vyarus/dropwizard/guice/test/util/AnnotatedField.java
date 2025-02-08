package ru.vyarus.dropwizard.guice.test.util;

import org.junit.platform.commons.util.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * Annotated field wrapper. Used to simplify work with test fields by hiding all required reflection.
 *
 * @param <A> annotation type
 * @param <T> value type
 * @author Vyacheslav Rusakov
 * @since 07.02.2025
 */
public class AnnotatedField<A extends Annotation, T> {
    private final A annotation;
    private final Field field;
    private final Class<?> testClass;

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
        try {
            return (T) field.get(instance);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Failed to get field " + toStringField()
                    + " value", e);
        }
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
        try {
            field.set(instance, value);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Failed to set field " + toStringField() + " value to " + value, e);
        }
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
     * @return to string field with class and reduced package
     */
    public String toStringField() {
        return TestFieldUtils.toString(field);
    }

    @Override
    public String toString() {
        return toStringField() + " ("
                + (isStatic() ? "static " : "") + "@" + annotation.annotationType().getSimpleName()
                + " " + field.getType().getSimpleName()
                + ")";
    }
}
