package ru.vyarus.dropwizard.guice.test.util.support;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.inject.Injector;
import io.dropwizard.configuration.ConfigurationSourceProvider;
import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.DropwizardTestSupport;
import javax.annotation.Nullable;
import ru.vyarus.dropwizard.guice.hook.GuiceyConfigurationHook;
import ru.vyarus.dropwizard.guice.test.GuiceyTestSupport;
import ru.vyarus.dropwizard.guice.test.TestSupport;
import ru.vyarus.dropwizard.guice.test.util.ConfigOverrideUtils;
import ru.vyarus.dropwizard.guice.test.util.HooksUtil;
import ru.vyarus.dropwizard.guice.test.util.RandomPortsListener;
import ru.vyarus.dropwizard.guice.test.util.client.DefaultTestClientFactory;
import ru.vyarus.dropwizard.guice.test.util.client.TestClientFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Builder and runner for {@link io.dropwizard.testing.DropwizardTestSupport} and
 * {@link ru.vyarus.dropwizard.guice.test.GuiceyTestSupport} objects. Allows using all available options. This builder
 * should be suitable for cases when junit 5 extensions could not be used.
 * <p>
 * Use {@link ru.vyarus.dropwizard.guice.test.TestSupport#build(Class)} to build instance.
 *
 * @param <C> configuration type
 * @author Vyacheslav Rusakov
 * @since 14.11.2023
 */
@SuppressWarnings({"PMD.TooManyMethods", "PMD.GodClass", "PMD.AvoidFieldNameMatchingMethodName"})
public class TestSupportBuilder<C extends Configuration> {

    private final Class<? extends Application<C>> app;
    private String configPath;
    private ConfigurationSourceProvider configSourceProvider;
    private final Map<String, Supplier<String>> configOverrides = new HashMap<>();
    private C configObject;
    private String propertyPrefix;
    private boolean randomPorts;
    private String restMapping;
    private final List<TestListener<C>> listeners = new ArrayList<>();
    private TestClientFactory factory = new DefaultTestClientFactory();

    public TestSupportBuilder(final Class<? extends Application<C>> app) {
        this.app = app;
    }

    /**
     * Must not be used if {@link #config(io.dropwizard.core.Configuration)} used.
     *
     * @param path configuration file path
     * @return builder instance for chained calls
     */
    public TestSupportBuilder<C> config(final @Nullable String path) {
        this.configPath = path;
        return this;
    }

    /**
     * Use configuration instance instead of configuration parsing from yaml file. When this is used, other
     * configuration options must not be used (they can't be used, and an error would be thrown indicating incorrect
     * usage).
     *
     * @param config pre-initialized configuration object
     * @return builder instance for chained calls
     */
    public TestSupportBuilder<C> config(final @Nullable C config) {
        this.configObject = config;
        return this;
    }

    /**
     * Must not be used if {@link #config(io.dropwizard.core.Configuration)} used.
     *
     * @param provider configuration source provider
     * @return builder instance for chained calls
     */
    public TestSupportBuilder<C> configSourceProvider(final @Nullable ConfigurationSourceProvider provider) {
        this.configSourceProvider = provider;
        return this;
    }

    /**
     * Must not be used if {@link #config(io.dropwizard.core.Configuration)} used.
     *
     * @param overrides config override values (in format "path: value")
     * @return builder instance for chained calls
     */
    public TestSupportBuilder<C> configOverrides(final String... overrides) {
        for (String over : overrides) {
            configOverride(over);
        }
        return this;
    }

    /**
     * Must not be used if {@link #config(io.dropwizard.core.Configuration)} used.
     *
     * @param override config override value (in format "path: value")
     * @return builder instance for chained calls
     */
    public TestSupportBuilder<C> configOverride(final @Nullable String override) {
        if (override != null) {
            final int idx = override.indexOf(':');
            Preconditions.checkState(idx > 0,
                    "Incorrect configuration override declaration: must be 'key: value', but found '%s'", override);
            configOverride(override.substring(0, idx).trim(), override.substring(idx + 1).trim());
        }
        return this;
    }

    /**
     * Must not be used if {@link #config(io.dropwizard.core.Configuration)} used.
     *
     * @param key   configuration path
     * @param value overriding value
     * @return builder instance for chained calls
     */
    public TestSupportBuilder<C> configOverride(final String key, final String value) {
        return configOverride(key, () -> value);
    }

    /**
     * Must not be used if {@link #config(io.dropwizard.core.Configuration)} used.
     *
     * @param key   configuration path
     * @param value overriding value provider
     * @return builder instance for chained calls
     */
    public TestSupportBuilder<C> configOverride(final String key, final Supplier<String> value) {
        this.configOverrides.put(key, value);
        return this;
    }

    /**
     * Dropwizard stored all provided configuration overriding values as system properties with provided prefix
     * (or "dw." by default). If multiple tests run concurrently, they would collide on using the same system
     * properties. It is preferred to specify test-unique prefix.
     *
     * @param prefix configuration override properties prefix
     * @return builder instance for chained calls
     */
    public TestSupportBuilder<C> propertyPrefix(final @Nullable String prefix) {
        this.propertyPrefix = prefix;
        return this;
    }

    /**
     * Shortcut to enable random web ports.
     *
     * @return builder instance for chained calls
     */
    public TestSupportBuilder<C> randomPorts() {
        return randomPorts(true);
    }

    /**
     * Use random http ports (applicable only for web). Useful to separate concurrent web instances runs.
     *
     * @return builder instance for chained calls
     */
    public TestSupportBuilder<C> randomPorts(final boolean randomPorts) {
        this.randomPorts = randomPorts;
        return this;
    }

    /**
     * Specifies rest mapping path. This is the same as specifying direct config override
     * {@code "server.rootMapping: /something/*"}. Specified value would be prefixed with "/" and, if required
     * "/*" applied at the end. So it would be correct to specify {@code restMapping = "api"} (actually set value
     * would be "/api/*").
     * <p>
     * This option is only intended to simplify cases when custom configuration file is not yet used in tests
     * (usually early PoC phase). It allows you to map servlet into application root in test (because rest is no
     * more resided in root). When used with existing configuration file, this parameter will override file definition.
     *
     * @return builder instance for chained calls
     */
    public TestSupportBuilder<C> restMapping(final String restMapping) {
        this.restMapping = restMapping;
        return this;
    }

    /**
     * Custom client factory implementation used for {@link ru.vyarus.dropwizard.guice.test.ClientSupport} object
     * creation (this special client class automatically constructs base urls for application under test,
     * based on its configuration).
     * <p>
     * Client instance could be accessed at any time (during test) with {@link TestSupportHolder#getClient()}
     *
     * @param factory factory instance
     * @return builder instance for chained calls
     */
    public TestSupportBuilder<C> clientFactory(final TestClientFactory factory) {
        this.factory = factory;
        return this;
    }

    /**
     * Listener used ONLY when builder run methods used! Listener may be used to perform additional initialization
     * or cleanup before/after application execution.
     *
     * @param listener execution listener
     * @return builder instance for chained calls
     */
    public TestSupportBuilder<C> listen(final TestListener<C> listener) {
        this.listeners.add(listener);
        return this;
    }

    /**
     * Shortcut for hooks registration (method simply immediately registers provided hooks).
     *
     * @param hooks hook classes to install (nulls not allowed)
     * @return builder instance for chained calls
     */
    @SafeVarargs
    public final TestSupportBuilder<C> hooks(final Class<? extends GuiceyConfigurationHook>... hooks) {
        HooksUtil.register(HooksUtil.create(hooks));
        return this;
    }

    /**
     * Shortcut for hooks registration (method simply immediately registers provided hooks).
     *
     * @param hooks hooks to install (nulls allowed)
     * @return builder instance for chained calls
     */
    public TestSupportBuilder<C> hooks(final GuiceyConfigurationHook... hooks) {
        HooksUtil.register(Arrays.asList(hooks));
        return this;
    }

    /**
     * Build a test support object with web services {@link io.dropwizard.testing.DropwizardTestSupport}.
     * Method supposed to be used only by {@link ru.vyarus.dropwizard.guice.test.TestSupport} for support objects
     * creation. Prefer direct run ({@link #runCore()}) method usage (used support object could be easily obtained
     * with {@link ru.vyarus.dropwizard.guice.test.TestSupport#getContext()} in any place).
     * <p>
     * IMPORTANT: listeners could not be used (because they are implemented as a custom run callback)
     *
     * @return guicey test support implementation
     */
    public GuiceyTestSupport<C> buildCore() {
        if (!listeners.isEmpty()) {
            throw new IllegalStateException("Listeners could be used only with run* methods.");
        }
        return buildCoreInternal();
    }

    /**
     * Build a test support object with web services {@link io.dropwizard.testing.DropwizardTestSupport}.
     * Method supposed to be used only by {@link ru.vyarus.dropwizard.guice.test.TestSupport} for support objects
     * creation. Prefer direct run ({@link #runWeb()}) method usage (used support object could be easily obtained
     * with {@link ru.vyarus.dropwizard.guice.test.TestSupport#getContext()} in any place).
     * <p>
     * IMPORTANT: listeners could not be used (because they are implemented as a custom run callback)
     *
     * @return dropwizard test support implementation
     */
    public DropwizardTestSupport<C> buildWeb() {
        if (!listeners.isEmpty()) {
            throw new IllegalStateException("Listeners could be used only with run* methods.");
        }
        return buildWebInternal();
    }

    /**
     * Start and stop application without web services. Mostly useful to test application startup errors
     * (with proper application shutdown).
     *
     * @return test support object used for execution (for example, to examine configuration)
     * @throws Exception any appeared exception (throws may easily be added directly to test method and, without
     *                   extra exception wrapper, we get exact exceptions as they would be thrown in real application)
     */
    public DropwizardTestSupport<C> runCore() throws Exception {
        return runCore(injector -> TestSupport.getContext());
    }

    /**
     * Start and stop application without web services. Provided action would be executed in time of application life.
     *
     * @param action action to execute while the application is running
     * @param <T>    result type
     * @return action result
     * @throws Exception any appeared exception (throws may easily be added directly to test method and, without
     *                   extra exception wrapper, we get exact exceptions as they would be thrown in real application)
     */
    public <T> T runCore(final @Nullable TestSupport.RunCallback<T> action) throws Exception {
        return run(buildCoreInternal(), action);
    }

    /**
     * Start and stop application with web services. Mostly useful to test application startup errors
     * (with proper application shutdown).
     *
     * @return test support object used for execution (for example, to examine configuration)
     * @throws Exception any appeared exception (throws may easily be added directly to test method and, without
     *                   extra exception wrapper, we get exact exceptions as they would be thrown in real application)
     */
    public DropwizardTestSupport<C> runWeb() throws Exception {
        return runWeb(injector -> TestSupport.getContext());
    }

    /**
     * Start and stop application with web services. Provided action would be executed in time of application life.
     *
     * @param action action to execute while the application is running
     * @param <T>    result type
     * @return action result
     * @throws Exception any appeared exception (throws may easily be added directly to test method and, without
     *                   extra exception wrapper, we get exact exceptions as they would be thrown in real application)
     */
    public <T> T runWeb(final @Nullable TestSupport.RunCallback<T> action) throws Exception {
        return run(buildWebInternal(), action);
    }

    // "unsafe" building (without listeners check)
    private GuiceyTestSupport<C> buildCoreInternal() {
        final GuiceyTestSupport<C> support;
        if (configObject != null) {
            if (configPath != null || !configOverrides.isEmpty() || configSourceProvider != null) {
                throw new IllegalStateException("Configuration object can't be used together with yaml configuration");
            }
            support = new GuiceyTestSupport<>(app, configObject);
        } else {
            final String prefix = MoreObjects.firstNonNull(propertyPrefix, "dw.");
            support = new GuiceyTestSupport<>(app, configPath, configSourceProvider, prefix, prepareOverrides(prefix));
        }
        if (randomPorts) {
            support.addListener(new RandomPortsListener<>());
        }

        return support;
    }

    // "unsafe" building (without listeners check)
    private DropwizardTestSupport<C> buildWebInternal() {
        final DropwizardTestSupport<C> support;
        if (configObject != null) {
            if (configPath != null || !configOverrides.isEmpty() || configSourceProvider != null) {
                throw new IllegalStateException("Configuration object can't be used together with yaml configuration");
            }
            support = new DropwizardTestSupport<>(app, configObject);
        } else {
            final String prefix = MoreObjects.firstNonNull(propertyPrefix, "dw.");
            support = new DropwizardTestSupport<>(app, configPath, configSourceProvider,
                    prefix, prepareOverrides(prefix));
        }
        if (randomPorts) {
            support.addListener(new RandomPortsListener<>());
        }

        return support;
    }

    private ConfigOverride[] prepareOverrides(final String prefix) {
        final ConfigOverride[] override = new ConfigOverride[configOverrides.size() + (restMapping == null ? 0 : 1)];
        int i = 0;
        for (Map.Entry<String, Supplier<String>> entry : configOverrides.entrySet()) {
            override[i++] = ConfigOverride.config(prefix,
                    entry.getKey(), entry.getValue());
        }
        if (restMapping != null) {
            override[i] = ConfigOverrideUtils.overrideRestMapping(prefix, restMapping);
        }
        return override;
    }

    @SuppressWarnings("PMD.AvoidRethrowingException")
    private <T> T run(final DropwizardTestSupport<C> support,
                      final @Nullable TestSupport.RunCallback<T> callback) throws Exception {
        return runWithListeners(support, callback);
    }

    private <T> T runWithListeners(final DropwizardTestSupport<C> support,
                                   final @Nullable TestSupport.RunCallback<T> callback) throws Exception {
        // setup (before run)
        for (TestListener<C> testListener : listeners) {
            testListener.setup(support);
        }
        try {
            // using TestSupport for running because this api was added before builder and can't be moved into
            // builder (without breaking change)
            return TestSupport.run(support, factory, injector -> {
                // after app startup, before the main test logic run
                for (TestListener<C> testListener : listeners) {
                    testListener.run(support, injector);
                }
                try {
                    return callback != null ? callback.run(injector) : null;
                } finally {
                    // after the main test logic run (app still running)
                    for (TestListener<C> listener : listeners) {
                        listener.stop(support, injector);
                    }
                }
            });
        } finally {
            // after application shutdown
            for (TestListener<C> listener : listeners) {
                listener.cleanup(support);
            }
        }
    }

    /**
     * Listener for {@link ru.vyarus.dropwizard.guice.test.TestSupport#build(Class)} builder. Listener works only when
     * builder run method used! Useful for test-specific setup and cleanup. Note that
     * {@link ru.vyarus.dropwizard.guice.test.GuiceyTestSupport} extends
     * {@link io.dropwizard.testing.DropwizardTestSupport}. Guicey support object does not provide any useful
     * methods (in context of the builder), so it is appropriate to always use dropwizard support object.
     * <p>
     * See {@link ru.vyarus.dropwizard.guice.test.TestSupport} utility methods if something guice-related is
     * required. To access web client use {@link ru.vyarus.dropwizard.guice.test.TestSupport#getContextClient()}
     *
     * @param <C> configuration type
     */
    public interface TestListener<C extends Configuration> {
        /**
         * Called before application startup.
         *
         * @param support initialized support object (not started)
         * @throws Exception any errors pass through
         */
        default void setup(final DropwizardTestSupport<C> support) throws Exception {
            // empty
        }

        /**
         * An application started, but test logic was not executed yet. Will not be called in case of
         * application startup error.
         * <p>
         * {@link ru.vyarus.dropwizard.guice.test.ClientSupport} web client could be accessed with
         * {@link ru.vyarus.dropwizard.guice.test.TestSupport#getContextClient()}.
         *
         * @param support  started support object
         * @param injector injector instance
         * @throws Exception any errors pass through
         */
        default void run(final DropwizardTestSupport<C> support, final Injector injector) throws Exception {
            //empty
        }

        /**
         * Called after test action (or after exception during action execution), but before application shutdown.
         * <p>
         * {@link ru.vyarus.dropwizard.guice.test.ClientSupport} web client could be accessed with
         * {@link ru.vyarus.dropwizard.guice.test.TestSupport#getContextClient()}.
         *
         * @param support  still started suport object
         * @param injector injector instance
         * @throws Exception any errors pass through
         */
        default void stop(final DropwizardTestSupport<C> support, final Injector injector) throws Exception {
            // empty
        }

        /**
         * Called after application shutdown (including startup error case).
         *
         * @param support stopped support object
         * @throws Exception any errors pass through
         */
        default void cleanup(final DropwizardTestSupport<C> support) throws Exception {
            // empty
        }
    }
}
