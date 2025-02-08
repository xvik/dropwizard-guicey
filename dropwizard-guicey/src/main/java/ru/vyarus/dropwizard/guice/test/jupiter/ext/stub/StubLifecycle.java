package ru.vyarus.dropwizard.guice.test.jupiter.ext.stub;

/**
 * Helper interface for {@link ru.vyarus.dropwizard.guice.test.jupiter.ext.stub.StubBean}. For stubs, implementing
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
