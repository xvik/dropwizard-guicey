package ru.vyarus.dropwizard.guice.test.jupiter.env;

/**
 * Extension for guicey junit 5 test extensions ({@link ru.vyarus.dropwizard.guice.test.jupiter.TestDropwizardApp}
 * and {@link ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp}). Called before test support object and
 * test application creation. Provides additional abilities to configure test.
 * <p>
 * Useful for management of additional environment objects like embedded database and
 * overriding test application configuration. Consider this as a simpler option to writing custom junit extensions.
 * <p>
 * If you need to take action after test execution (e.g. shutdown database) then return {@link java.lang.AutoCloseable}
 * or {@link org.junit.jupiter.api.extension.ExtensionContext.Store.CloseableResource} object, and it would be
 * closed automatically.
 * <p>
 * If auto close is not enough, use
 * {@link ru.vyarus.dropwizard.guice.test.jupiter.env.TestExtension#listen(
 * ru.vyarus.dropwizard.guice.test.jupiter.env.listen.TestExecutionListener)} listener for reacting on exact test
 * phases (or lambda-based listener methods: on*).
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
 * Setup objects do not duplicate all hook methods, instead a new hook could be registered from the setup object
 * (e.g., if you need extension context access in hook - you should register a setup object and then create hook
 * (inside it) providing entire junit context or just some stored values.
 * <p>
 * For complex extensions it is recommended to implement hook
 * ({@link ru.vyarus.dropwizard.guice.hook.GuiceyConfigurationHook}) and/or listener
 * ({@link ru.vyarus.dropwizard.guice.test.jupiter.env.listen.TestExecutionListener}) interfaces directly
 * (and register them as {@code .hooks(this).listen(this)}).
 * <p>
 * Environment setup could be loaded with {@link java.util.ServiceLoader} to avoid manual registration: add
 * {@code META-INF/services/ru.vyarus.dropwizard.guice.test.jupiter.env.TestEnvironmentSetup} file with one or more
 * implementation classes (one per line).
 *
 * @author Vyacheslav Rusakov
 * @since 12.05.2022
 */
@FunctionalInterface
public interface TestEnvironmentSetup {

    /**
     * Called before test application startup under junit "before all" phase or "before each" (depends on extension
     * registration). Assumed to be used for starting additional test objects (like embedded database) and application
     * configuration (configuration overrides).Provided object allow you to provide direct configuration overrides
     * (e.g. to override database credentials).
     * <p>
     * For simplicity, any non closable returned object simply ignored. This was done to simplify lambas usage:
     * {@code TestEnvironmentSetup env = ext -> ext.configOverrides("foo:1")} - here configuration object
     * would be implicitly returned (because all methods return object itself for chained calls) and ignored.
     *
     * @param extension test extension configuration object (support chained calls)
     * @return {@link java.lang.AutoCloseable} or
     * {@link org.junit.jupiter.api.extension.ExtensionContext.Store.CloseableResource} if something needs to be
     * shut down after test, any other object would be ignored (including null)
     *
     * @throws java.lang.Exception on error (to simplify usage)
     */
    Object setup(TestExtension extension) throws Exception;
}
