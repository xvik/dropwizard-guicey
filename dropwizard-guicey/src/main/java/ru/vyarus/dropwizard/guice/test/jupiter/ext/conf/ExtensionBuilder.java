package ru.vyarus.dropwizard.guice.test.jupiter.ext.conf;

import io.dropwizard.testing.ConfigOverride;
import org.junit.jupiter.api.extension.ExtensionContext;
import ru.vyarus.dropwizard.guice.hook.GuiceyConfigurationHook;
import ru.vyarus.dropwizard.guice.test.util.ConfigOverrideExtensionValue;
import ru.vyarus.dropwizard.guice.test.util.ConfigOverrideUtils;
import ru.vyarus.dropwizard.guice.test.util.ConfigOverrideValue;
import ru.vyarus.dropwizard.guice.test.util.ConfigurablePrefix;
import ru.vyarus.dropwizard.guice.test.client.TestClientFactory;

import java.util.Collections;
import java.util.function.Supplier;

/**
 * Base class for {@link ru.vyarus.dropwizard.guice.test.jupiter.ext.TestDropwizardAppExtension},
 * {@link ru.vyarus.dropwizard.guice.test.jupiter.ext.TestGuiceyAppExtension} and
 * {@link ru.vyarus.dropwizard.guice.test.jupiter.env.TestEnvironmentSetup} builders (to avoid duplicating
 * method implementations).
 *
 * @param <C> config object type
 * @param <T> builder type
 * @author Vyacheslav Rusakov
 * @since 12.05.2022
 */
public abstract class ExtensionBuilder<T extends ExtensionBuilder, C extends ExtensionConfig> {
    protected final C cfg;

    public ExtensionBuilder(final C cfg) {
        this.cfg = cfg;
    }

    /**
     * Specifies configuration overrides pairs in format: {@code "key: value"}. Might be called multiple times
     * (values appended).
     * <p>
     * Note that overrides order is not predictable so don't specify multiple values for the same property
     * (see {@link io.dropwizard.testing.DropwizardTestSupport} holds overrides in {@link java.util.Set}).
     *
     * @param values overriding configuration values in "key: value" format
     * @return builder instance for chained calls
     * @see #configOverrides(io.dropwizard.testing.ConfigOverride...)
     * for using {@link io.dropwizard.testing.ConfigOverride} objects directly
     */
    public T configOverrides(final String... values) {
        cfg.configOverrides = ConfigOverrideUtils.mergeRaw(cfg.configOverrides, values);
        return self();
    }

    /**
     * Direct {@link io.dropwizard.testing.ConfigOverride} objects support. In most cases, it is simpler to use
     * pure strings with {@link #configOverrides(String...)}. Direct objects may be useful when provided value must
     * be lazy evaluated (e.g. it is obtained from some other junit extension).
     * <p>
     * IMPORTANT: provided values must implement {@link ru.vyarus.dropwizard.guice.test.util.ConfigurablePrefix}
     * interface so guicey could insert correct prefix, used by current test (required for parallel tests as all
     * config overrides eventually stored in system properties).
     * <p>
     * May be called multiple times (values appended).
     *
     * @param values overriding configuration values
     * @param <K>    value type
     * @return builder instance for chained calls
     * @see ru.vyarus.dropwizard.guice.test.util.ConfigOverrideValue for an exmample of required implementation
     * @see #configOverride(String, java.util.function.Supplier) for supplier shortcut
     */
    @SafeVarargs
    public final <K extends ConfigOverride & ConfigurablePrefix> T configOverrides(final K... values) {
        Collections.addAll(cfg.configOverrideObjects, values);
        return self();
    }

    /**
     * Register config override with a supplier. Useful for values with delayed resolution
     * (e.g. provided by some other extension).
     * <p>
     * Note that overrides order is not predictable so don't specify multiple values for the same property
     * (see {@link io.dropwizard.testing.DropwizardTestSupport} holds overrides in {@link java.util.Set}).
     *
     * @param key      configuration key
     * @param supplier value supplier
     * @return builder instance for chained calls
     */
    public T configOverride(final String key, final Supplier<String> supplier) {
        configOverrides(new ConfigOverrideValue(key, supplier));
        return self();
    }

    /**
     * Shortcut for {@link #configOverrideByExtension(
     * org.junit.jupiter.api.extension.ExtensionContext.Namespace, String, String)} for cases when storage key
     * and configuration path is the same. If possible, prefer this method for simplicity.
     *
     * @param namespace junit storage namespace to resolve value in
     * @param key       value name in namespace and overriding property name
     * @return builder instance for chained calls
     */
    public T configOverrideByExtension(final ExtensionContext.Namespace namespace, final String key) {
        return configOverrideByExtension(namespace, key, key);
    }

    /**
     * Override configuration value from 3rd party junit extension. Such value must be stored by
     * extension in the junit store with provided namespace (for simple cases use
     * {@link org.junit.jupiter.api.extension.ExtensionContext.Namespace#GLOBAL}). It is advised to use the same
     * storage key as configuration path (for simplicity). Value must be initialized in
     * {@link org.junit.jupiter.api.extension.BeforeAllCallback} because guicey initialize config overrides
     * under this stage.
     * <p>
     * WARNING: keep in mind that your extension must be executed before guicey because otherwise value would
     * not be taken into account. To highlight such cases, guicey would put a warning in logs indicating
     * absent value in configured storage.
     * <p>
     * Such complication is required for a very special cases when parallel tests execution must be used together
     * with some common extension (for example, starting database) declared in base class with a static field.
     * Using test storage is the only way to guarantee different values in parallel tests.
     * <p>
     * As an alternative, you can use {@link ru.vyarus.dropwizard.guice.test.jupiter.env.TestEnvironmentSetup}
     * implementation, registered directly into guicey extensions (would be called exactly before and after test
     * support object creation and destruction).
     *
     * @param namespace  junit storage namespace to resolve value in
     * @param storageKey value name in namespace
     * @param configPath overriding property name
     * @return builder instance for chained calls
     */
    public T configOverrideByExtension(final ExtensionContext.Namespace namespace,
                                       final String storageKey,
                                       final String configPath) {
        configOverrides(new ConfigOverrideExtensionValue(namespace, storageKey, configPath));
        return self();
    }

    /**
     * Hooks provide access to guice builder allowing application-level customization of application context in tests.
     * <p>
     * Anonymous implementation could be simply declared as field:
     * {@code @EnableHook static GuiceyConfigurationHook hook = builder -> builder.disableExtension(
     * Something.class)}.
     * Non-static fields may be used only when extension is registered with non-static field (static fields would
     * be also counted in this case). All annotated fields will be detected automatically and objects registered.
     * Fields declared in base test classes are also counted.
     *
     * @param hooks hook classes to use
     * @return builder instance for chained calls
     */
    @SafeVarargs
    public final T hooks(final Class<? extends GuiceyConfigurationHook>... hooks) {
        cfg.hookClasses(hooks);
        return self();
    }

    /**
     * May be used for quick configurations with lambda:
     * <pre>{@code
     * .hooks(builder -> builder.modules(new DebugModule()))
     * }</pre>
     * May be called multiple times (values appended).
     * <p>
     * Anonymous implementation could be simply declared as field:
     * {@code @EnableHook static GuiceyConfigurationHook hook = builder -> builder.disableExtension(
     * Something.class)}.
     * Non-static fields may be used only when extension is registered with non-static field (static fields would
     * be also counted in this case). All annotated fields will be detected automatically and objects registered.
     * Fields declared in base test classes are also counted.
     *
     * @param hooks hook instances (may be lambdas)
     * @return builder instance for chained calls
     */
    public T hooks(final GuiceyConfigurationHook... hooks) {
        cfg.hookInstances(hooks);
        return self();
    }

    /**
     * When test lifecycle is {@link org.junit.jupiter.api.TestInstance.Lifecycle#PER_CLASS} same test instance
     * used for all test methods. By default, guicey would perform fields injection before each method because
     * there might be prototype beans that must be refreshed for each test method. If you don't rely on
     * prototypes, injections could be performed just once (for the first test method).
     *
     * @return builder instance for chained calls
     */
    public T injectOnce() {
        cfg.injectOnce = true;
        return self();
    }

    /**
     * Enables debug output for extension: used setup objects, hooks and applied config overrides. Might be useful
     * for concurrent tests too because each message includes configuration prefix (exactly pointing to context test
     * or method).
     * <p>
     * Also, shows guicey extension time, so if you suspect that guicey spent too much time, use the debug option to
     * be sure. Performance report is published after each "before each" phase and after "after all" to let you
     * see how extension time increased with each test method (for non-static guicey extension (executed per method),
     * performance printed after "before each" and "after each" because before/after all not available)
     * <p>
     * Configuration overrides are printed after application startup (but before the test) because overridden values
     * are resolved from system properties (applied by {@link io.dropwizard.testing.DropwizardTestSupport#before()}).
     * If application startup failed, no configuration overrides would be printed (because dropwizard would immediately
     * clean up system properties). Using system properties is the only way to receive actually applied configuration
     * value because property overrides might be implemented as value providers and potentially return different values.
     * <p>
     * System property might be used to enable debug mode: {@code -Dguicey.extensions.debug=true}. Or alias in code:
     * {@link ru.vyarus.dropwizard.guice.test.TestSupport#debugExtensions()}.
     *
     * @return builder instance for chained calls
     */
    public T debug() {
        cfg.tracker.debug = true;
        return self();
    }

    /**
     * By default, a new application instance is started for each test. If you want to re-use the same application
     * instance between several tests, then put extension declaration in BASE test class and enable the reuse option:
     * all tests derived from this base class would use the same application instance.
     * <p>
     * You may have multiple base classes with reusable application declaration (different test hierarchies) - in
     * this case, multiple applications would be kept running during tests execution.
     * <p>
     * All other extensions (without enabled re-use) will start new applications: take this into account to
     * prevent port clashes with already started reusable apps.
     * <p>
     * Reused application instance would be stopped after all tests execution.
     *
     * @return builder instance for chained calls
     */
    public T reuseApplication() {
        cfg.reuseApp = true;
        return self();
    }

    /**
     * Default extensions: {@link ru.vyarus.dropwizard.guice.test.jupiter.ext.mock.MockBean},
     * {@link ru.vyarus.dropwizard.guice.test.jupiter.ext.stub.StubBean},
     * {@link ru.vyarus.dropwizard.guice.test.jupiter.ext.spy.SpyBean},
     * {@link ru.vyarus.dropwizard.guice.test.jupiter.ext.track.TrackBean}.
     * <p>
     * By default, these extensions enabled and this option could disable them (if there are problems with them or
     * fields analysis took too much time).
     *
     * @return builder instance for chained calls
     */
    public T disableDefaultExtensions() {
        cfg.defaultExtensionsEnabled = false;
        return self();
    }

    /**
     * Use custom jersey client builder for {@link ru.vyarus.dropwizard.guice.test.ClientSupport} object.
     *
     * @param factory factory implementation
     * @return builder instance for chained calls
     */
    public T clientFactory(final TestClientFactory factory) {
        cfg.clientFactory = factory;
        return self();
    }

    @SuppressWarnings("unchecked")
    private T self() {
        return (T) this;
    }
}
