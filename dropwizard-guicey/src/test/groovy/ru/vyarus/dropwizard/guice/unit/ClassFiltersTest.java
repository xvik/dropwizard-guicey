package ru.vyarus.dropwizard.guice.unit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBundle;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.function.Predicate;

import static ru.vyarus.dropwizard.guice.test.util.ClassFilters.*;

/**
 * @author Vyacheslav Rusakov
 * @since 28.03.2025
 */
public class ClassFiltersTest {

    @Test
    void testAnnotated() {
        final Predicate<Class<?>> annotated = annotated(Ann1.class, Ann2.class);
        Assertions.assertTrue(annotated.test(Root1.InnerAnn1.class));
        Assertions.assertTrue(annotated.test(Root1.InnerAnn2.class));
        Assertions.assertFalse(annotated.test(Root1.InnerSkip1.class));
        Assertions.assertFalse(annotated.test(Root1.InnerNoAnn.class));
    }

    @Test
    void testPackages() {
        final Predicate<Class<?>> packg = inPackages(ClassFiltersTest.class.getPackage().getName());
        Assertions.assertTrue(packg.test(Root1.class));
        Assertions.assertTrue(packg.test(Root1.InnerAnn1.class));
        Assertions.assertFalse(packg.test(GuiceyBundle.class));
    }

    @Test
    void testDeclaredIn() {
        final Predicate<Class<?>> declared = declaredIn(Root1.class);
        Assertions.assertTrue(declared.test(Root1.InnerAnn1.class));
        Assertions.assertTrue(declared.test(Root1.InnerAnn2.class));
        Assertions.assertFalse(declared.test(Root2.InnerNoAnn.class));
    }

    @Test
    void testIgnoreAnnotated() {
        final Predicate<Class<?>> ignoreAnn = ignoreAnnotated(Skip1.class, Skip2.class);
        Assertions.assertTrue(ignoreAnn.test(Root1.InnerAnn1.class));
        Assertions.assertFalse(ignoreAnn.test(Root1.InnerSkip1.class));
        Assertions.assertFalse(ignoreAnn.test(Root1.InnerSkip2.class));
        Assertions.assertTrue(ignoreAnn.test(Root1.InnerNoAnn.class));
    }

    @Test
    void testIgnorePackages() {
        final Predicate<Class<?>> ignorePackg = ignorePackages(ClassFiltersTest.class.getPackage().getName());
        Assertions.assertFalse(ignorePackg.test(Root1.class));
        Assertions.assertFalse(ignorePackg.test(Root1.InnerAnn1.class));
        Assertions.assertTrue(ignorePackg.test(GuiceyBundle.class));
    }

    @Test
    void testIgnoreDeclaredIn() {
        final Predicate<Class<?>> ignoreDeclared = ignoreDeclaredIn(Root1.class);
        Assertions.assertFalse(ignoreDeclared.test(Root1.InnerAnn1.class));
        Assertions.assertTrue(ignoreDeclared.test(Root2.InnerNoAnn.class));
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface Ann1 {}

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface Ann2 {}

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface Skip1 {}

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface Skip2 {}

    public static class Root1 {

        @Ann1
        public static class InnerAnn1 {}

        @Ann2
        public static class InnerAnn2 {}

        @Skip1
        public static class InnerSkip1 {}

        @Skip2
        public static class InnerSkip2 {}

        public static class InnerNoAnn {}
    }

    public static class Root2 {
        public static class InnerNoAnn {}
    }
}
