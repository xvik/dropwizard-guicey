package ru.vyarus.dropwizard.guice.test.jupiter.env.field;

import org.junit.platform.commons.support.AnnotationSupport;
import ru.vyarus.dropwizard.guice.debug.util.RenderUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Helper utility for search and processing annotated test fields (for test extensions implementation).
 *
 * @author Vyacheslav Rusakov
 * @since 07.02.2025
 */
public final class TestFieldUtils {

    private TestFieldUtils() {
    }

    /**
     * Search for annotated fields in test clas (including base test hierarchy). In the returned list static fields
     * go first (sort applied).
     *
     * @param testClass    test class
     * @param ann          field annotation
     * @param requiredType required field type (if field is not a supertype of declared - error thrown)
     * @param <A>          annotation type
     * @param <T>          field (base) type
     * @return detected fields
     */
    public static <A extends Annotation, T> List<AnnotatedField<A, T>> findAnnotatedFields(
            final Class<?> testClass,
            final Class<A> ann,
            final Class<T> requiredType) {
        final List<Field> fields = AnnotationSupport.findAnnotatedFields(testClass, ann);

        final List<AnnotatedField<A, T>> res = new ArrayList<>(fields.size());
        for (Field field : fields) {
            if (!requiredType.isAssignableFrom(field.getType())) {
                throw new IllegalStateException(String.format(
                        "Field %s annotated with @%s, but its type is not %s",
                        toString(field), ann.getSimpleName(), requiredType.getSimpleName()
                ));
            }
            res.add(new AnnotatedField<>(field.getAnnotation(ann), field, testClass));
        }
        // sort static fields first
        res.sort(Comparator.comparing(field -> field.isStatic() ? 0 : 1));
        return res;
    }

    /**
     * Filter fields, declared in base test classes (not own fields).
     *
     * @param fields fields to filter
     * @param <A>    annotation type
     * @param <T>    field type
     * @return fields not directly declared in test class
     */
    public static <A extends Annotation, T> List<AnnotatedField<A, T>> getInheritedFields(
            final List<AnnotatedField<A, T>> fields) {
        return fields.stream()
                .filter(fieldAccess -> !fieldAccess.isTestOwnField())
                .collect(Collectors.toList());
    }

    /**
     * Filter fields, declared directly in test classes (own fields).
     *
     * @param fields fields to filter
     * @param <A>    annotation type
     * @param <T>    field type
     * @return fields directly declared in test class
     */
    public static <A extends Annotation, T> List<AnnotatedField<A, T>> getTestOwnFields(
            final List<AnnotatedField<A, T>> fields) {
        return fields.stream()
                .filter(AnnotatedField::isTestOwnField)
                .collect(Collectors.toList());
    }

    /**
     * Get multiple fields value at once.
     *
     * @param fields   fields to get values from
     * @param instance test instance (could by null for static fields)
     * @param <A>      annotation type
     * @param <T>      field type
     * @return field values
     */
    public static <A extends Annotation, T> List<T> getValues(
            final List<AnnotatedField<A, T>> fields, final Object instance) {
        final List<T> res = new ArrayList<>();
        for (final AnnotatedField<A, T> field : fields) {
            res.add(field.getValue(instance));
        }
        return res;
    }

    /**
     * @param field field
     * @return string field representation with class and reduced package (for reports and logs).
     */
    public static String toString(final Field field) {
        return RenderUtils.renderClass(field.getDeclaringClass()) + "." + field.getName();
    }
}
