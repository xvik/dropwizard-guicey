package ru.vyarus.dropwizard.guice.test.jupiter.env;

/**
 * Extension for guicey junit 5 test extensions ({@link ru.vyarus.dropwizard.guice.test.jupiter.TestDropwizardApp}
 * and {@link ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp}). Called before test application
 * execution. Useful for management of additional environment objects like embedded database and
 * overriding test application configuration. Consider this as a simpler option to writing custom junit extensions.
 * <p>
 * If you need to take action after test execution (e.g. shutdown database) then return {@link java.lang.AutoCloseable}
 * or {@link org.junit.jupiter.api.extension.ExtensionContext.Store.CloseableResource} object, and it would be
 * closed automatically.
 * <p>
 * The same could be achieved with an additional junit 5 extensions, but it might be harder to properly synchronize
 * lifecycles (extensions order would be important). Environment support assumed to be a simpler alternative.
 * <p>
 * Setup object might be registered directly into extension annotation or with extension builder (when extension
 * registered with field). Also, support object may be declared in field (in test or any base test class),
 * annotated with {@link ru.vyarus.dropwizard.guice.test.jupiter.env.EnableSetup} annotation (annotation is required
 * to provide context javadoc).
 * <p>
 * To avoid confusion with guicey hooks: setup object required to prepare test environment before test (and apply
 * required configurations) whereas hooks is a general mechanism for application customization (not only in tests).
 *
 * @author Vyacheslav Rusakov
 * @since 12.05.2022
 */
@FunctionalInterface
public interface TestEnvironmentSetup {

    /**
     * Called before test application startup under junit "before all" phase. Assumed to be used for starting
     * additional test objects (like embedded database) and application configuration (configuration overrides).
     * Provided object allow you to provide direct configuration overrides (e.g. to override database credentials).
     * <p>
     * For simplicity, any non closable returned object simply ignored. This was done to simplify lambas usage:
     * {@code TestEnvironmentSetup env = ext -> ext.configOverrides("foo:1")} - here configuration object
     * would be implicitly returned (because all methods return object itself for chained calls) and ignored.
     *
     * @param extension test extension configuration object (support chained calls)
     * @return {@link java.lang.AutoCloseable} or
     * {@link org.junit.jupiter.api.extension.ExtensionContext.Store.CloseableResource} if something needs to be
     * shut down after test, any other object would be ignored (including null)
     */
    Object setup(TestExtension extension);
}
