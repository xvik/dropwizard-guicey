package ru.vyarus.dropwizard.guice.test.jupiter.ext.mock;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Replace any guice service with mockito mock in test (using guice module overrides).
 * <p>
 * Important: requires mockito dependency!
 * <p>
 * Example: {@code @MockBean Service mock}. May be used for static and instance fields.
 * <p>
 * Note: could be used for spy objects creation: {@code @MockBean Service spy = Mockito.spy(instance)}. This might
 * be useful in cases when service bound by instance and automatic spy (@SpyBean) can't be used.
 * <p>
 * Mock field might be initialized manually: {@code @MockBean Service mock = Mockito.mock(Service.class)}.
 * Manual mocks in instance field must be synchronized with the correct guicey extension declaration (by default,
 * injector created per test class and test instance created per method, so it is impossible to "see" mocks declared
 * in instance fields). Incorrect usage would be immediately reported with error.
 * <p>
 * Mock stubs could be configured in test beforeEach method: {@code Mockito.when(mock).something().thenReturn("ok")}.
 * <p>
 * Note that you can use {@link ru.vyarus.dropwizard.guice.test.jupiter.ext.stub.StubBean} with manual mock
 * initialization with the almost same result, except it would not be cleared before each test.
 * <p>
 * Mocks reset called after each test method. Could be disabled with {@link #autoReset()}
 * <p>
 * Mockito provide the detailed report of used mock methods and redundant stub definitions. Use {@link #printSummary()}
 * to enable this report (printed after each test method).
 * <p>
 * Guicey extension debug ({@link ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp#debug()}) enables
 * mock fields debug: all recognized annotated fields would be printed to console.
 * <p>
 * Limitation: any aop, applied to the original bean, will not work with mock (because guice can't apply aop to
 * instances)! Use {@link ru.vyarus.dropwizard.guice.test.jupiter.ext.spy.SpyBean} instead if aop is important.
 * Does not work for HK2 beans.
 *
 * @author Vyacheslav Rusakov
 * @since 10.02.2025
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface MockBean {

    /**
     * Note: mock could be reset manually with {@link org.mockito.Mockito#reset(Object[])}.
     *
     * @return true to reset mock after each test method
     */
    boolean autoReset() default true;

    /**
     * Native mockito mock usage report: shows called methods and stubbed, but not used methods.
     *
     * @return true to print mock summary after each test
     */
    boolean printSummary() default false;
}
