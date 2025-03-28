package ru.vyarus.dropwizard.guice.test.util;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.function.Predicate;

/**
 * Simple class {@link java.util.function.Predicate} implementations.
 * Supposed to be used with
 * {@link ru.vyarus.dropwizard.guice.GuiceBundle.Builder#autoConfigFilter(java.util.function.Predicate)}.
 *
 * @author Vyacheslav Rusakov
 * @since 28.03.2025
 */
public final class ClassFilters {

    private ClassFilters() {
    }

    /**
     * @param annotation annotations to check
     * @return predicate accepting classes, annotated with (at least) one of the provided annotations.
     */
    @SafeVarargs
    public static Predicate<Class<?>> annotated(final Class<? extends Annotation>... annotation) {
        return type -> Arrays.stream(annotation)
                .anyMatch(type::isAnnotationPresent);
    }

    /**
     * @param packages packages to accept
     * @return predicate accepting classes in provided packages
     */
    public static Predicate<Class<?>> inPackages(final String... packages) {
        return type -> Arrays.stream(packages)
                .anyMatch(pkg -> type.getName().startsWith(pkg));
    }

    /**
     * This might be useful for tests to limit classpath scan by test class only.
     *
     * @param baseClasses base (declaration) classes
     * @return predicate accepting classes declared in one of the provided base classes
     */
    public static Predicate<Class<?>> declaredIn(final Class<?>... baseClasses) {
        return type -> type.getDeclaringClass() != null && Arrays.stream(baseClasses)
                .anyMatch(base -> type.getDeclaringClass().equals(base));
    }

    /**
     * @param annotation annotations to check
     * @return predicate accepting classes not annotated with any of the provided annotations
     */
    @SafeVarargs
    public static Predicate<Class<?>> ignoreAnnotated(final Class<? extends Annotation>... annotation) {
        return annotated(annotation).negate();
    }

    /**
     * @param packages packages to ignore
     * @return predicate accepting classes not from the declared packages
     */
    public static Predicate<Class<?>> ignorePackages(final String... packages) {
        return inPackages(packages).negate();
    }

    /**
     * @param baseClasses base (declaration) classes
     * @return predicate accepting classes not declared in one of the provided base classes
     */
    public static Predicate<Class<?>> ignoreDeclaredIn(final Class<?>... baseClasses) {
        return declaredIn(baseClasses).negate();
    }
}
