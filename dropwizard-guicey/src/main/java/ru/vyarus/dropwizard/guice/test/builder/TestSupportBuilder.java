package ru.vyarus.dropwizard.guice.test.builder;

import com.google.common.base.MoreObjects;
import com.google.inject.Injector;
import io.dropwizard.core.Application;
import io.dropwizard.core.Configuration;
import io.dropwizard.core.cli.Command;
import io.dropwizard.core.server.AbstractServerFactory;
import io.dropwizard.testing.DropwizardTestSupport;
import jakarta.annotation.Nullable;
import ru.vyarus.dropwizard.guice.test.GuiceyTestSupport;
import ru.vyarus.dropwizard.guice.test.TestSupport;
import ru.vyarus.dropwizard.guice.test.client.ApacheTestClientFactory;
import ru.vyarus.dropwizard.guice.test.client.DefaultTestClientFactory;
import ru.vyarus.dropwizard.guice.test.client.TestClientFactory;
import ru.vyarus.dropwizard.guice.test.util.ConfigOverrideUtils;
import ru.vyarus.dropwizard.guice.test.util.RandomPortsListener;
import ru.vyarus.dropwizard.guice.test.util.RunResult;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Builder and runner for {@link io.dropwizard.testing.DropwizardTestSupport} and
 * {@link ru.vyarus.dropwizard.guice.test.GuiceyTestSupport} objects. Allows using all available options. This builder
 * should be suitable for cases when junit 5 extensions could not be used.
 * <p>
 * Use {@link ru.vyarus.dropwizard.guice.test.TestSupport#build(Class)} to build instance.
 * <p>
 * Builder is not supposed to be used for multiple runs: registered hooks will be applied only once. This limitation
 * is not possible to avoid because builder could be used for support objects creation, which are not aware of
 * hooks. So hooks could be registered globally only in time of addition to the builder.
 *
 * @param <C> configuration type
 * @author Vyacheslav Rusakov
 * @since 14.11.2023
 */
@SuppressWarnings("PMD.AvoidFieldNameMatchingMethodName")
public class TestSupportBuilder<C extends Configuration> extends BaseBuilder<C, TestSupportBuilder<C>> {

    private boolean randomPorts;
    private final List<TestListener<C>> listeners = new ArrayList<>();
    private TestClientFactory factory = new DefaultTestClientFactory();

    /**
     * Create builder.
     *
     * @param app application type
     */
    public TestSupportBuilder(final Class<? extends Application<C>> app) {
        super(app);
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
     * @param randomPorts true to use random ports
     * @return builder instance for chained calls
     */
    public TestSupportBuilder<C> randomPorts(final boolean randomPorts) {
        this.randomPorts = randomPorts;
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
     * Shortcut for {@link #clientFactory(ru.vyarus.dropwizard.guice.test.client.TestClientFactory)} to configure
     * {@link ru.vyarus.dropwizard.guice.test.client.ApacheTestClientFactory}. The default
     * {@link org.glassfish.jersey.client.HttpUrlConnectorProvider} supports only HTTP 1.1 methods and have
     * problem with PATCH method usage on jdk &gt; 16.
     *
     * @return builder instance for chained calls
     */
    public TestSupportBuilder<C> apacheClient() {
        return clientFactory(new ApacheTestClientFactory());
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
     * Build a test support object with web services {@link io.dropwizard.testing.DropwizardTestSupport}.
     * Method supposed to be used only by {@link ru.vyarus.dropwizard.guice.test.TestSupport} for support objects
     * creation. Prefer direct run ({@link #runCore()}) method usage (used support object could be easily obtained
     * with {@link ru.vyarus.dropwizard.guice.test.TestSupport#getContext()} in any place).
     * <p>
     * IMPORTANT: listeners could not be used (because they are implemented as a custom run callback).
     * Custom {@link ru.vyarus.dropwizard.guice.test.client.TestClientFactory} would also be lost! Use direct run
     * methods to not lose them.
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
     * IMPORTANT: listeners could not be used (because they are implemented as a custom run callback).
     * Custom {@link ru.vyarus.dropwizard.guice.test.client.TestClientFactory} would also be lost! Use direct run
     * methods to not lose them.
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
     * <p>
     * NOTE: method not supposed to be used for multiple calls. For example, registered hooks would only work
     * on first execution.
     *
     * @return action result
     * @throws Exception any appeared exception (throws may easily be added directly to test method and, without
     *                   extra exception wrapper, we get exact exceptions as they would be thrown in real application)
     */
    public RunResult<C> runCore() throws Exception {
        return runCore(injector -> new RunResult<C>(TestSupport.getContext(), injector));
    }

    /**
     * Start and stop application without web services. Provided action would be executed in time of application life.
     * <p>
     * NOTE: method not supposed to be used for multiple calls. For example, registered hooks would only work
     * on first execution.
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
     * Start and stop application without web services. Provided action would be executed in time of application life.
     * Does not simulate {@link io.dropwizard.lifecycle.Managed} objects lifecycle (start/stop would not be called).
     * <p>
     * NOTE: method not supposed to be used for multiple calls. For example, registered hooks would only work
     * on first execution.
     *
     * @return execution result (with all required objects for verification)
     * @throws Exception any appeared exception (throws may easily be added directly to test method and, without
     *                   extra exception wrapper, we get exact exceptions as they would be thrown in real application)
     */
    public RunResult<C> runCoreWithoutManaged() throws Exception {
        return runCoreWithoutManaged(injector -> new RunResult<C>(TestSupport.getContext(), injector));
    }

    /**
     * Start and stop application without web services. Provided action would be executed in time of application life.
     * Does not simulate {@link io.dropwizard.lifecycle.Managed} objects lifecycle (start/stop would not be called).
     * <p>
     * NOTE: method not supposed to be used for multiple calls. For example, registered hooks would only work
     * on first execution.
     *
     * @param action action to execute while the application is running
     * @param <T>    result type
     * @return action result
     * @throws Exception any appeared exception (throws may easily be added directly to test method and, without
     *                   extra exception wrapper, we get exact exceptions as they would be thrown in real application)
     */
    public <T> T runCoreWithoutManaged(final @Nullable TestSupport.RunCallback<T> action) throws Exception {
        return run(buildCoreInternal().disableManagedLifecycle(), action);
    }


    /**
     * Start and stop application with web services. Mostly useful to test application startup errors
     * (with proper application shutdown).
     * <p>
     * NOTE: method not supposed to be used for multiple calls. For example, registered hooks would only work
     * on first execution.
     *
     * @return result action
     * @throws Exception any appeared exception (throws may easily be added directly to test method and, without
     *                   extra exception wrapper, we get exact exceptions as they would be thrown in real application)
     */
    public RunResult<C> runWeb() throws Exception {
        return runWeb(injector -> new RunResult<C>(TestSupport.getContext(), injector));
    }

    /**
     * Start and stop application with web services. Provided action would be executed in time of application life.
     * <p>
     * NOTE: method not supposed to be used for multiple calls. For example, registered hooks would only work
     * on first execution.
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
        support.configModifiers(modifiers);
        if (randomPorts) {
            support.addListener(new RandomPortsListener<>());
        }

        return support;
    }

    // "unsafe" building (without listeners check)
    private DropwizardTestSupport<C> buildWebInternal() {
        final DropwizardTestSupport<C> support;
        if (configObject != null && restMapping != null && !restMapping.isEmpty()) {
            // rest mapping can't be applied with config override in case of a raw config object,
            // so need to use modifier instead
            configModifiers(config -> ((AbstractServerFactory) config.getServerFactory())
                    .setJerseyRootPath(ConfigOverrideUtils.formatRestMapping(restMapping)));
        }
        final Function<Application<C>, Command> cmd = ConfigOverrideUtils.buildCommandFactory(modifiers);
        if (configObject != null) {
            if (configPath != null || !configOverrides.isEmpty() || configSourceProvider != null) {
                throw new IllegalStateException("Configuration object can't be used together with yaml configuration");
            }
            support = new DropwizardTestSupport<>(app, configObject, cmd);
        } else {
            final String prefix = MoreObjects.firstNonNull(propertyPrefix, "dw.");
            support = new DropwizardTestSupport<>(app, configPath, configSourceProvider,
                    prefix, cmd, prepareOverrides(prefix));
        }
        if (randomPorts) {
            support.addListener(new RandomPortsListener<>());
        }

        return support;
    }

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
