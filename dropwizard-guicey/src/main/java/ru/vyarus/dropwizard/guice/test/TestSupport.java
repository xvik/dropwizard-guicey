package ru.vyarus.dropwizard.guice.test;

import com.google.inject.Injector;
import com.google.inject.Key;
import io.dropwizard.core.Application;
import io.dropwizard.core.Configuration;
import io.dropwizard.testing.DropwizardTestSupport;
import ru.vyarus.dropwizard.guice.injector.lookup.InjectorLookup;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.conf.TestExtensionsTracker;

import jakarta.annotation.Nullable;

/**
 * Utility class combining test-framework agnostic utilities.
 * <ul>
 *     <li>{@link DropwizardTestSupport} factory
 *     <li>{@link GuiceyTestSupport} factory (same as previous but without web part starting)
 *     <li>{@link ClientSupport} factory (web client)
 *     <li>Guice-related utilities like {@link Injector} or beans lookup
 *     <li>Utility methods for running before and after methods in one call (useful for error situation testing).
 * </ul>
 *
 * @author Vyacheslav Rusakov
 * @since 09.02.2022
 */
public final class TestSupport {

    private TestSupport() {
    }

    /**
     * Creates {@link DropwizardTestSupport} instance for application configured from configuration file.
     * {@link DropwizardTestSupport} starts complete dropwizard application including web part. Suitable
     * for testing rest or servlet endpoints. For web-less application start see {@link #coreApp(Class, String)}.
     * <p>
     * Note: this is just a most common use-case, for more complex cases instantiate object manually using
     * different constructor.
     *
     * @param appClass   application class
     * @param configPath configuration file path (absolute or relative to working dir) (may be null)
     * @param <C>        configuration type
     * @return dropwizard test support instance
     */
    public static <C extends Configuration> DropwizardTestSupport<C> webApp(
            final Class<? extends Application<C>> appClass, final @Nullable String configPath) {
        return new DropwizardTestSupport<>(appClass, configPath);
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
     * @param <C>        configuration type
     * @return guicey test support instance
     */
    public static <C extends Configuration> GuiceyTestSupport<C> coreApp(
            final Class<? extends Application<C>> appClass, final @Nullable String configPath) {
        return new GuiceyTestSupport<>(appClass, configPath);
    }

    /**
     * Factory method for creating helper web client. Client is aware of dropwizard configuration and allows
     * easy calling main/rest/admin contexts. Could also be used as a generic web client (for remote endpoints calls).
     * <p>
     * Note that instance must be closed after usage, for example with try-with-resources:
     * {@code try(ClientSupport client = TestSupport.webClient(support)) {...}}.
     *
     * @param support test support object (dropwizard or guicey)
     * @return client support instance
     */
    public static ClientSupport webClient(final DropwizardTestSupport<?> support) {
        return new ClientSupport(support);
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
     * Normally, {@link DropwizardTestSupport#before()} and {@link DropwizardTestSupport#after()} methods are called
     * separately. This method is a shortcut mostly for errors testing when {@link DropwizardTestSupport#before()}
     * assumed to fail to make sure {@link DropwizardTestSupport#after()} will be called in any case.
     *
     * @param callback callback (may be null)
     * @param <T>      result type
     * @param support  test support instance
     * @return callback result
     * @throws Exception any appeared exception
     */
    public static <T> T run(final DropwizardTestSupport<?> support,
                            final @Nullable RunCallback<T> callback) throws Exception {
        support.before();
        try {
            return callback != null ? callback.run(getInjector(support)) : null;
        } finally {
            support.after();
        }
    }

    /**
     * Shortcut for web application startup test (replacing
     * {@code TestSupport.execute(TestSupport.webApp(App.class, path)}).
     *
     * @param appClass   application class
     * @param configPath configuration file path (absolute or relative to working dir) (may be null)
     * @param <C>        configuration type
     * @throws Exception any appeared exception
     */
    public static <C extends Configuration> void runWebApp(final Class<? extends Application<C>> appClass,
                                                           final @Nullable String configPath) throws Exception {
        runWebApp(appClass, configPath, null);
    }

    /**
     * Shortcut for web application startup test (replacing
     * {@code TestSupport.execute(TestSupport.webApp(App.class, path), callback)}).
     *
     * @param appClass   application class
     * @param configPath configuration file path (absolute or relative to working dir) (may be null)
     * @param callback   callback to execute while application started (may be null)
     * @param <C>        configuration type
     * @param <T>        result type
     * @return callback result
     * @throws Exception any appeared exception
     */
    public static <T, C extends Configuration> T runWebApp(final Class<? extends Application<C>> appClass,
                                                           final @Nullable String configPath,
                                                           final @Nullable RunCallback<T> callback)
            throws Exception {
        return run(webApp(appClass, configPath), callback);
    }

    /**
     * Shortcut for core application startup test (replacing
     * {@code TestSupport.execute(TestSupport.coreApp(App.class, path))}).
     *
     * @param appClass   application class
     * @param configPath configuration file path (absolute or relative to working dir) (may be null)
     * @param <C>        configuration type
     * @throws Exception any appeared exception
     */
    public static <C extends Configuration> void runCoreApp(final Class<? extends Application<C>> appClass,
                                                            final @Nullable String configPath) throws Exception {
        runCoreApp(appClass, configPath, null);
    }

    /**
     * Shortcut for core application startup test (replacing
     * {@code TestSupport.execute(TestSupport.coreApp(App.class, path), callback)}).
     *
     * @param appClass   application class
     * @param configPath configuration file path (absolute or relative to working dir) (may be null)
     * @param callback   callback to execute while application started (may be null)
     * @param <C>        configuration type
     * @param <T>        result type
     * @return callback result
     * @throws Exception any appeared exception
     */
    public static <T, C extends Configuration> T runCoreApp(final Class<? extends Application<C>> appClass,
                                                            final @Nullable String configPath,
                                                            final @Nullable RunCallback<T> callback)
            throws Exception {
        return run(coreApp(appClass, configPath), callback);
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
