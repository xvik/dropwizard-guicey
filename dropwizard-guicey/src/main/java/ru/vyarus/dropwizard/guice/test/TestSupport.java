package ru.vyarus.dropwizard.guice.test;

import com.google.inject.Injector;
import com.google.inject.Key;
import io.dropwizard.core.Application;
import io.dropwizard.core.Configuration;
import io.dropwizard.testing.DropwizardTestSupport;
import javax.annotation.Nullable;
import ru.vyarus.dropwizard.guice.injector.lookup.InjectorLookup;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.conf.TestExtensionsTracker;
import ru.vyarus.dropwizard.guice.test.util.client.TestClientFactory;
import ru.vyarus.dropwizard.guice.test.util.support.TestSupportBuilder;
import ru.vyarus.dropwizard.guice.test.util.support.TestSupportHolder;

/**
 * Utility class combining test-framework agnostic utilities.
 * <ul>
 *     <li>{@link DropwizardTestSupport} factory
 *     <li>{@link GuiceyTestSupport} factory (same as previous but without web part starting)
 *     <li>{@link ClientSupport} factory (web client)
 *     <li>Guice-related utilities like {@link Injector} or beans lookup
 *     <li>Utility methods for running before and after methods in one call (useful for error situation testing).
 *     <li>Utilities for accessing context {@link io.dropwizard.testing.DropwizardTestSupport} object for both
 *     manually running tests (with run method below) or junit extensions</li>
 * </ul>
 *
 * @author Vyacheslav Rusakov
 * @since 09.02.2022
 */
public final class TestSupport {

    private TestSupport() {
    }

    /**
     * Generic builder to build and run application (core or web). This is the most flexible way to build or run
     * the support object (with all possible options). In simple cases, prefer direct methods
     * like {@link #coreApp(Class, String, String...)} or {@link #runCoreApp(Class, String, String...)} (all
     * these methods are builder shortcuts).
     * <p>
     * Should be useful for testing without a custom test framework integration as it provides lifecycle listener
     * support to simplify setup and cleaup actions.
     *
     * @param app application class
     * @param <C> configuration type
     * @return test support object builder
     */
    public static <C extends Configuration> TestSupportBuilder<C> build(final Class<? extends Application<C>> app) {
        return new TestSupportBuilder<>(app);
    }

    /**
     * Obtains a context support object used by test application running in the current thread.
     * Works for manual runs (using run* methods below) and junit extension runs.
     *
     * @return support object running application
     * @throws java.lang.IllegalStateException if context support object is not registered for the current thread
     */
    public static <C extends Configuration> DropwizardTestSupport<C> getContext() {
        return TestSupportHolder.getContext();
    }

    /**
     * Obtains a context client instance used by test application running in the current thread.
     * Works for manual runs (using run* methods below) and junit extension runs.
     *
     * @return client instance (in case of junit extensions - same instance)
     * @throws java.lang.IllegalStateException if context support object is not registered for the current thread
     */
    public static ClientSupport getContextClient() {
        return TestSupportHolder.getClient();
    }

    /**
     * Creates {@link DropwizardTestSupport} instance for application configured from configuration file.
     * {@link DropwizardTestSupport} starts complete dropwizard application including web part. Suitable
     * for testing rest or servlet endpoints. For web-less application start see
     * {@link #coreApp(Class, String, String...)}.
     * <p>
     * Note: this is just a most common use-case, for more complex cases instantiate object manually using
     * different constructor.
     *
     * @param appClass   application class
     * @param configPath configuration file path (absolute or relative to working dir) (may be null)
     * @param overrides  config override values (in format "path: value")
     * @param <C>        configuration type
     * @return dropwizard test support instance
     */
    public static <C extends Configuration> DropwizardTestSupport<C> webApp(
            final Class<? extends Application<C>> appClass,
            final @Nullable String configPath,
            final String... overrides) {
        return build(appClass)
                .config(configPath)
                .configOverrides(overrides)
                .buildWeb();
    }

    /**
     * Creates {@link GuiceyTestSupport} instance for application configured from configuration file. It is
     * pre-configured {@link DropwizardTestSupport} instance (derivative class) starting only core application
     * part (guice context) without web part. Suitable for testing core logic.
     * <p>
     * Note: this is just a most common use-case, for more complex cases instantiate object manually using
     * different constructor.
     *
     * @param appClass   application class
     * @param configPath configuration file path (absolute or relative to working dir) (may be null)
     * @param overrides  config override values (in format "path: value")
     * @param <C>        configuration type
     * @return guicey test support instance
     */
    public static <C extends Configuration> GuiceyTestSupport<C> coreApp(
            final Class<? extends Application<C>> appClass,
            final @Nullable String configPath,
            final String... overrides) {
        return build(appClass)
                .config(configPath)
                .configOverrides(overrides)
                .buildCore();
    }

    /**
     * Factory method for creating a helper web client. The client is aware of dropwizard configuration and allows
     * easy calling main/rest/admin contexts. It could also be used as a generic web client (for remote endpoints
     * calls).
     * <p>
     * Note that instance must be closed after usage, for example, with try-with-resources:
     * {@code try(ClientSupport client = TestSupport.webClient(support)) {...}}.
     *
     * @param support test support object (dropwizard or guicey)
     * @return client support instance
     */
    public static ClientSupport webClient(final DropwizardTestSupport<?> support) {
        return new ClientSupport(support);
    }

    /**
     * Helper web client creation with custom jersey client factory (to configure client differently).
     * Note that {@link ClientSupport} is still useful in this case because it automatically constructs
     * urls for tested application (based on configuration).
     *
     * @param support test support object (dropwizard or guicey)
     * @param factory configuration factory
     * @return client support instance
     */
    public static ClientSupport webClient(final DropwizardTestSupport<?> support, final TestClientFactory factory) {
        return new ClientSupport(support, factory);
    }

    /**
     * @param support test support object (dropwizard or guicey)
     * @return application injector instance
     */
    public static Injector getInjector(final DropwizardTestSupport<?> support) {
        return InjectorLookup.getInjector(support.getApplication())
                .orElseThrow(() -> new IllegalStateException("Injector not available"));
    }

    /**
     * Shortcut for accessing guice beans.
     *
     * @param support test support object (dropwizard or guicey)
     * @param type    target bean type
     * @param <T>     bean type
     * @return bean instance
     */
    public static <T> T getBean(final DropwizardTestSupport<?> support, final Class<T> type) {
        return getBean(support, Key.get(type));
    }

    /**
     * Shortcut for accessing guice beans.
     *
     * @param support test support object (dropwizard or guicey)
     * @param key     binding key
     * @param <T>     bean type
     * @return bean instance
     */
    public static <T> T getBean(final DropwizardTestSupport<?> support, final Key<T> key) {
        return getInjector(support).getInstance(key);
    }

    /**
     * Shortcut method to apply field injections into target object instance. Useful to initialize test class
     * fields (under not supported test frameworks).
     *
     * @param support test support object (dropwizard or guicey)
     * @param target  target instance to inject beans
     */
    public static void injectBeans(final DropwizardTestSupport<?> support, final Object target) {
        getInjector(support).injectMembers(target);
    }

    /**
     * Shortcut for {@link #run(io.dropwizard.testing.DropwizardTestSupport,
     * ru.vyarus.dropwizard.guice.test.TestSupport.RunCallback)}.
     *
     * @param support test support instance
     * @throws Exception any appeared exception (throws may easily be added directly to test method and, without
     *                   extra exception wrapper, we get exact exceptions as they would be thrown in real application)
     */
    public static void run(final DropwizardTestSupport<?> support) throws Exception {
        run(support, null);
    }

    /**
     * Shortcut for {@link #run(io.dropwizard.testing.DropwizardTestSupport,
     * ru.vyarus.dropwizard.guice.test.util.client.TestClientFactory,
     * ru.vyarus.dropwizard.guice.test.TestSupport.RunCallback)}.
     *
     * @param callback callback (may be null)
     * @param <T>      result type
     * @param support  test support instance
     * @return callback result
     * @throws Exception any appeared exception (throws may easily be added directly to test method and, without
     *                   extra exception wrapper, we get exact exceptions as they would be thrown in real application)
     */
    public static <T> T run(final DropwizardTestSupport<?> support,
                            @Nullable final RunCallback<T> callback) throws Exception {
        return run(support, null, callback);
    }


    /**
     * Normally, {@link DropwizardTestSupport#before()} and {@link DropwizardTestSupport#after()} methods are called
     * separately. This method is a shortcut mostly for errors testing when {@link DropwizardTestSupport#before()}
     * assumed to fail to make sure {@link DropwizardTestSupport#after()} will be called in any case.
     *
     * @param callback      callback (may be null)
     * @param <T>           result type
     * @param support       test support instance
     * @param clientFactory custom client factory for {@link ru.vyarus.dropwizard.guice.test.ClientSupport} object
     * @return callback result
     * @throws Exception any appeared exception (throws may easily be added directly to test method and, without
     *                   extra exception wrapper, we get exact exceptions as they would be thrown in real application)
     */
    public static <T> T run(final DropwizardTestSupport<?> support,
                            final @Nullable TestClientFactory clientFactory,
                            final @Nullable RunCallback<T> callback) throws Exception {
        try {
            TestSupportHolder.setContext(support, clientFactory);
            support.before();
            return callback != null ? callback.run(getInjector(support)) : null;
        } finally {
            support.after();
            TestSupportHolder.reset();
        }
    }

    /**
     * Shortcut for web application startup.
     *
     * @param appClass application class
     * @param <C>      configuration type
     * @return test support object used for execution (for example, to examine configuration)
     * @throws Exception any appeared exception (throws may easily be added directly to test method and, without
     *                   extra exception wrapper, we get exact exceptions as they would be thrown in real application)
     */
    public static <C extends Configuration> DropwizardTestSupport<C> runWebApp(
            final Class<? extends Application<C>> appClass) throws Exception {
        return runWebApp(appClass, null, injector -> TestSupport.getContext());
    }

    /**
     * Shortcut for web application startup.
     *
     * @param appClass application class
     * @param callback callback to execute while application started (may be null)
     * @param <C>      configuration type
     * @param <T>      result type
     * @return callback result
     * @throws Exception any appeared exception (throws may easily be added directly to test method and, without
     *                   extra exception wrapper, we get exact exceptions as they would be thrown in real application)
     */
    public static <T, C extends Configuration> T runWebApp(
            final Class<? extends Application<C>> appClass,
            final @Nullable RunCallback<T> callback) throws Exception {
        return runCoreApp(appClass, null, callback);
    }

    /**
     * Shortcut for web application startup with configuration (optional).
     *
     * @param appClass   application class
     * @param configPath configuration file path (absolute or relative to working dir) (may be null)
     * @param <C>        configuration type
     * @return test support object used for execution (for example, to examine configuration)
     * @throws Exception any appeared exception (throws may easily be added directly to test method and, without
     *                   extra exception wrapper, we get exact exceptions as they would be thrown in real application)
     */
    public static <C extends Configuration> DropwizardTestSupport<C> runWebApp(
            final Class<? extends Application<C>> appClass,
            final @Nullable String configPath,
            final String... overrides) throws Exception {
        return runWebApp(appClass, configPath, injector -> TestSupport.getContext(), overrides);
    }

    /**
     * Shortcut for web application startup test (replacing
     * {@code TestSupport.execute(TestSupport.webApp(App.class, path), callback)}).
     *
     * @param appClass   application class
     * @param configPath configuration file path (absolute or relative to working dir) (may be null)
     * @param callback   callback to execute while application started (may be null)
     * @param overrides  config override values (in format "path: value")
     * @param <C>        configuration type
     * @param <T>        result type
     * @return callback result
     * @throws Exception any appeared exception (throws may easily be added directly to test method and, without
     *                   extra exception wrapper, we get exact exceptions as they would be thrown in real application)
     */
    public static <T, C extends Configuration> T runWebApp(final Class<? extends Application<C>> appClass,
                                                           final @Nullable String configPath,
                                                           final @Nullable RunCallback<T> callback,
                                                           final String... overrides) throws Exception {
        return run(webApp(appClass, configPath, overrides), callback);
    }

    /**
     * Shortcut for core application startup.
     *
     * @param appClass application class
     * @param <C>      configuration type
     * @return test support object used for execution (for example, to examine configuration)
     * @throws Exception any appeared exception (throws may easily be added directly to test method and, without
     *                   extra exception wrapper, we get exact exceptions as they would be thrown in real application)
     */
    public static <C extends Configuration> DropwizardTestSupport<C> runCoreApp(
            final Class<? extends Application<C>> appClass) throws Exception {
        return runCoreApp(appClass, injector -> TestSupport.getContext());
    }

    /**
     * Shortcut for core application startup.
     *
     * @param appClass application class
     * @param callback callback to execute while application started (may be null)
     * @param <C>      configuration type
     * @param <T>      result type
     * @return callback result
     * @throws Exception any appeared exception (throws may easily be added directly to test method and, without
     *                   extra exception wrapper, we get exact exceptions as they would be thrown in real application)
     */
    public static <T, C extends Configuration> T runCoreApp(
            final Class<? extends Application<C>> appClass,
            final @Nullable RunCallback<T> callback) throws Exception {
        return runCoreApp(appClass, null, callback);
    }

    /**
     * Shortcut for core application startup test (replacing
     * {@code TestSupport.execute(TestSupport.coreApp(App.class, path))}).
     *
     * @param appClass   application class
     * @param configPath configuration file path (absolute or relative to working dir) (may be null)
     * @param overrides  config override values (in format "path: value")
     * @param <C>        configuration type
     * @return test support object used for execution (for example, to examine configuration)
     * @throws Exception any appeared exception (throws may easily be added directly to test method and, without
     *                   extra exception wrapper, we get exact exceptions as they would be thrown in real application)
     */
    public static <C extends Configuration> DropwizardTestSupport<C> runCoreApp(
            final Class<? extends Application<C>> appClass,
            final @Nullable String configPath,
            final String... overrides) throws Exception {
        return runCoreApp(appClass, configPath, injector -> TestSupport.getContext(), overrides);
    }

    /**
     * Shortcut for core application startup test (replacing
     * {@code TestSupport.execute(TestSupport.coreApp(App.class, path), callback)}).
     *
     * @param appClass   application class
     * @param configPath configuration file path (absolute or relative to working dir) (may be null)
     * @param callback   callback to execute while application started (may be null)
     * @param overrides  config override values (in format "path: value")
     * @param <C>        configuration type
     * @param <T>        result type
     * @return callback result
     * @throws Exception any appeared exception (throws may easily be added directly to test method and, without
     *                   extra exception wrapper, we get exact exceptions as they would be thrown in real application)
     */
    public static <T, C extends Configuration> T runCoreApp(final Class<? extends Application<C>> appClass,
                                                            final @Nullable String configPath,
                                                            final @Nullable RunCallback<T> callback,
                                                            final String... overrides) throws Exception {
        return run(coreApp(appClass, configPath, overrides), callback);
    }

    /**
     * Enables debug output for registered junit 5 extensions. Simple alias for:
     * {@code System.setProperty("guicey.extensions.debug", "true")}.
     * <p>
     * Alternatively, debug could be enabled on extension directly with debug option.
     */
    public static void debugExtensions() {
        System.setProperty(TestExtensionsTracker.GUICEY_EXTENSIONS_DEBUG, "true");
    }

    /**
     * Callback interface used for utility run application methods in {@link TestSupport}.
     * <p>
     * Use {@link #getContext()} to access the context support object and {@link #getContextClient()} to access
     * the context client.
     *
     * @param <T> result type
     */
    @FunctionalInterface
    public interface RunCallback<T> {

        /**
         * Execute custom logic while application started (using {@link DropwizardTestSupport} or
         * {@link GuiceyTestSupport}).
         *
         * @param injector application injector
         * @return value or null
         * @throws Exception errors propagated
         */
        T run(Injector injector) throws Exception;
    }
}
