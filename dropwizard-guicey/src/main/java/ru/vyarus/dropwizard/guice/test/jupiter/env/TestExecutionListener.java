package ru.vyarus.dropwizard.guice.test.jupiter.env;

import com.google.inject.Injector;
import io.dropwizard.testing.DropwizardTestSupport;
import org.junit.jupiter.api.extension.ExtensionContext;
import ru.vyarus.dropwizard.guice.test.ClientSupport;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.GuiceyExtensionsSupport;

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
     * @param context  junit extension
     * @see #getSupport(org.junit.jupiter.api.extension.ExtensionContext)
     * @see #getInjector(org.junit.jupiter.api.extension.ExtensionContext)
     * @see #getBean(org.junit.jupiter.api.extension.ExtensionContext, Class)
     * @see #getClient(org.junit.jupiter.api.extension.ExtensionContext)
     */
    default void started(final ExtensionContext context) {
        // empty default
    }

    /**
     * IMPORTANT: this method MIGHT NOT BE CALLED at all in case if extension is registered under non-static field
     * (and so application created before each method).
     * Prefer {@link #started(org.junit.jupiter.api.extension.ExtensionContext)} instead, which is always called
     * (but not always under beforeAll),
     * <p>
     * Method could be useful if some action must be performed before each test (in case of nested tests or
     * global application when "start" would not be called for each test).
     *
     * @param context  junit extension
     * @see #getSupport(org.junit.jupiter.api.extension.ExtensionContext)
     * @see #getInjector(org.junit.jupiter.api.extension.ExtensionContext)
     * @see #getBean(org.junit.jupiter.api.extension.ExtensionContext, Class)
     * @see #getClient(org.junit.jupiter.api.extension.ExtensionContext)
     */
    default void beforeAll(final ExtensionContext context) {
        // empty default
    }

    /**
     * Called before each test method execution. Guice injections into test instance already performed.
     * Even if an application is created in beforeEach phase, this method would be called after application creation.
     *
     * @param context  junit extension
     * @see #getSupport(org.junit.jupiter.api.extension.ExtensionContext)
     * @see #getInjector(org.junit.jupiter.api.extension.ExtensionContext)
     * @see #getBean(org.junit.jupiter.api.extension.ExtensionContext, Class)
     * @see #getClient(org.junit.jupiter.api.extension.ExtensionContext)
     */
    default void beforeEach(final ExtensionContext context) {
        // empty default
    }

    /**
     * Called after each test method execution. Even if an application is closed on afterEach, this method would be
     * called before it.
     *
     * @param context  junit extension
     * @see #getSupport(org.junit.jupiter.api.extension.ExtensionContext)
     * @see #getInjector(org.junit.jupiter.api.extension.ExtensionContext)
     * @see #getBean(org.junit.jupiter.api.extension.ExtensionContext, Class)
     * @see #getClient(org.junit.jupiter.api.extension.ExtensionContext)
     */
    default void afterEach(final ExtensionContext context) {
        // empty default
    }

    /**
     * IMPORTANT: this method MIGHT NOT BE CALLED at all in case if extension is registered under non-static field
     * (and so the application is stopped after each method).
     * Prefer {@link #stopped(org.junit.jupiter.api.extension.ExtensionContext)} instead, which is always called (but
     * not always under afterAll),
     * <p>
     * Method could be useful if some action must be performed after each test (in case of nested tests or
     * global application when "stop" would not be called for each test).
     *
     * @param context  junit extension
     * @see #getSupport(org.junit.jupiter.api.extension.ExtensionContext)
     * @see #getInjector(org.junit.jupiter.api.extension.ExtensionContext)
     * @see #getBean(org.junit.jupiter.api.extension.ExtensionContext, Class)
     * @see #getClient(org.junit.jupiter.api.extension.ExtensionContext)
     */
    default void afterAll(final ExtensionContext context) {
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
     * @param context  junit extension context
     * @see #getSupport(org.junit.jupiter.api.extension.ExtensionContext)
     * @see #getInjector(org.junit.jupiter.api.extension.ExtensionContext)
     * @see #getBean(org.junit.jupiter.api.extension.ExtensionContext, Class)
     * @see #getClient(org.junit.jupiter.api.extension.ExtensionContext)
     */
    default void stopped(final ExtensionContext context) {
        // empty default
    }

    /**
     * Shortcut method to avoid passing support as a parameter in each method. Normally, it is impossible that support
     * would not be found (under called lifecycle methods)
     *
     * @param context junit extension context
     * @return dropwizard support object (or guicey support)
     * @throws IllegalStateException if the support object not found (should be impossible)
     */
    default DropwizardTestSupport<?> getSupport(final ExtensionContext context) {
        return GuiceyExtensionsSupport.lookupSupport(context)
                .orElseThrow(() -> new IllegalStateException("Test support not found"));
    }

    /**
     * Shortcut method to avoid passing injector as a parameter in each method. Normally, it is impossible that
     * injector would not be found (under called lifecycle methods)
     *
     * @param context junit extension context
     * @return injector instance
     * @throws IllegalStateException if the injector object not found (should be impossible)
     */
    default Injector getInjector(final ExtensionContext context) {
        return GuiceyExtensionsSupport.lookupInjector(context)
                .orElseThrow(() -> new IllegalStateException("Injector not found"));
    }

    /**
     * Shortcut to get bean directly from injector.
     *
     * @param context junit extension context
     * @param type    bean class
     * @param <T>     bean type
     * @return bean instance, never null (throw error if not found)
     */
    default <T> T getBean(final ExtensionContext context, final Class<T> type) {
        return getInjector(context).getInstance(type);
    }

    /**
     * Note that client is created even for pure guicey tests (in case if something external must be called).
     *
     * @param context junit extension context
     * @return client instance
     * @throws IllegalStateException if the client object not found (should be impossible)
     */
    default ClientSupport getClient(final ExtensionContext context) {
        return GuiceyExtensionsSupport.lookupClient(context)
                .orElseThrow(() -> new IllegalStateException("Client not found"));
    }
}
