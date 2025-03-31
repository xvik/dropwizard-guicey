package ru.vyarus.dropwizard.guice.test.jupiter.env.listen;

/**
 * Test listener allows listening for the main test events. There are no beforeAll and afterAll events directly
 * because guicey extension could be created either on beforeAll or in beforeEach (depends on test configuration).
 * <p>
 * Before/after test called before and after each test method. This could be used for custom setup/cleanup logic.
 * BeforeAll and afterAll might not be called - use with caution (depends on extension registration).
 * <p>
 * This interface provides a simple replacement for junit extension (synchronized with guicey lifecycle).
 *
 * @author Vyacheslav Rusakov
 * @since 07.02.2025
 */
public interface TestExecutionListener {

    /**
     * Called when dropwizard (or guicey) application started. It could be beforeAll or beforeEach phase
     * (if important, look {@link org.junit.jupiter.api.extension.ExtensionContext#getTestMethod()} to make sure).
     * Application could start/stop multiple times within one test class (if extension registered in non-static field).
     * <p>
     * NOTE: At this stage, injections not yet performed inside test instance.
     * <p>
     * This method could be used instead of beforeAll because normally extension is created under beforeAll, but
     * for extensions created under beforeEach - it would be impossible to notify about beforeAll anyway.
     *
     * @param context context object providing access to all required objects (junit context, injector,
     *                test support, etc.)
     * @throws java.lang.Exception on error
     */
    default void started(final EventContext context) throws Exception {
        // empty default
    }

    /**
     * IMPORTANT: this method MIGHT NOT BE CALLED at all in case if extension is registered under non-static field
     * (and so application created before each method).
     * Prefer {@link #started(ru.vyarus.dropwizard.guice.test.jupiter.env.listen.EventContext)} instead, which is
     * always called (but not always under beforeAll),
     * <p>
     * Method could be useful if some action must be performed before each test (in case of nested tests or
     * global application when "start" would not be called for each test).
     *
     * @param context context object providing access to all required objects (junit context, injector,
     *                test support, etc.)
     * @throws java.lang.Exception on error
     */
    default void beforeAll(final EventContext context) throws Exception {
        // empty default
    }

    /**
     * Called before each test method execution. Guice injections into test instance already performed.
     * Even if an application is created in beforeEach phase, this method would be called after application creation.
     *
     * @param context context object providing access to all required objects (junit context, injector,
     *                test support, etc.)
     * @throws java.lang.Exception on error
     */
    default void beforeEach(final EventContext context) throws Exception {
        // empty default
    }

    /**
     * Called after each test method execution. Even if an application is closed on afterEach, this method would be
     * called before it.
     *
     * @param context context object providing access to all required objects (junit context, injector,
     *                test support, etc.)
     * @throws java.lang.Exception on error
     */
    default void afterEach(final EventContext context) throws Exception {
        // empty default
    }

    /**
     * IMPORTANT: this method MIGHT NOT BE CALLED at all in case if extension is registered under non-static field
     * (and so the application is stopped after each method).
     * Prefer {@link #stopped(ru.vyarus.dropwizard.guice.test.jupiter.env.listen.EventContext)} instead, which is
     * always called (but not always under afterAll),
     * <p>
     * Method could be useful if some action must be performed after each test (in case of nested tests or
     * global application when "stop" would not be called for each test).
     *
     * @param context context object providing access to all required objects (junit context, injector,
     *                test support, etc.)
     * @throws java.lang.Exception on error
     */
    default void afterAll(final EventContext context) throws Exception {
        // empty default
    }

    /**
     * Called when dropwizard (or guicey) application stopped. It could be afterAll or afterEach phase
     * (if important, look {@link org.junit.jupiter.api.extension.ExtensionContext#getTestMethod()} to make sure).
     * Application could start/stop multiple times within one test class (if extension registered in non-static field).
     * <p>
     * Note that in case of global application usage or for nested tests this method might not be called because
     * application lifecycle would be managed by the top-most test.
     * <p>
     * This method could be used instead of afterAll because normally extension is stopped under afterAll, but
     * for extensions stopped under afterEach - it would be impossible to notify about afterAll anyway.
     *
     * @param context context object providing access to all required objects (junit context, injector,
     *                test support, etc.)
     * @throws java.lang.Exception on error
     */
    default void stopped(final EventContext context) throws Exception {
        // empty default
    }
}
