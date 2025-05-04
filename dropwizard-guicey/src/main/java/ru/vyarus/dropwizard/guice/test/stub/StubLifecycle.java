package ru.vyarus.dropwizard.guice.test.stub;

/**
 * Helper interface for lifecycle-aware stubs implementation. Works with junit 5
 * {@link ru.vyarus.dropwizard.guice.test.jupiter.ext.stub.StubBean} extension
 * and raw {@link ru.vyarus.dropwizard.guice.test.stub.StubsHook}. For stubs, implementing
 * this interface, before and after methods would be called before and after each test (to perform some
 * cleanups or reset state).
 *
 * @author Vyacheslav Rusakov
 * @since 07.02.2025
 */
public interface StubLifecycle {

    /**
     * Called before each test method.
     */
    default void before() {
        // empty by default
    }

    /**
     * Called after each test method.
     */
    default void after() {
        // empty by default
    }
}
