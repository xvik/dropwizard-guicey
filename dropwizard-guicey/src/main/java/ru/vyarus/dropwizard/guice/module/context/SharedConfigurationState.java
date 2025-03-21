package ru.vyarus.dropwizard.guice.module.context;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.inject.Injector;
import io.dropwizard.core.Application;
import io.dropwizard.core.Configuration;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;
import io.dropwizard.lifecycle.Managed;
import javax.inject.Provider;
import ru.vyarus.dropwizard.guice.module.context.option.Options;
import ru.vyarus.dropwizard.guice.module.installer.util.StackUtils;
import ru.vyarus.dropwizard.guice.module.yaml.ConfigurationTree;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Application-wide shared state assumed to be used in the configuration phase. For example,
 * in complex cases when bundles must share some global state (otherwise it would require to maintain
 * some {@link ThreadLocal} field in a bundle). But other cases could arise too. Intended to be used for very rare
 * cases: use it only if you can't avoid shared state in configuration time (for example, like the guicey gsp bundle,
 * which requires global configuration, accessible by multiple bundles).
 * <p>
 * Internally, guicey use it to store created {@link com.google.inject.Injector} and make it available statically
 * (see {@link ru.vyarus.dropwizard.guice.injector.lookup.InjectorLookup}).
 * <p>
 * Universal sharing place should simplify testing in the future: if a new global state appeared, there would be no
 * need to reset anything in tests (to clear state, for example, on testing errors). One "dirty" place to replace
 * all separate hacks.
 * <p>
 * Shared state could be accessed statically with {@link #get(Application)}, or within
 * {@link ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBundle}. Guicey hooks could use
 * {@link ru.vyarus.dropwizard.guice.GuiceBundle.Builder#withSharedState(java.util.function.Consumer)} to
 * access application state. Alternatively, dropwizard {@link Environment} may be used for resolution:
 * {@link #get(Environment)}.
 * <p>
 * During startup shared state instance could be obtained directly as {@link #getStartupInstance()}, but only
 * from application main thread. This might be required for places where neither application nor environment
 * object available. After guice bundle's run method finishes, startup instance is unlinked.
 * <p>
 * Classes are used as state keys to simplify usage (shared object class is the key).
 * Shared value could be set only once (to prevent complex situations with state substitutions). It is advised
 * to initialize shared value only in initialization phase (to avoid potential static access errors).
 * <p>
 * Objects available in shared state by default: {@link io.dropwizard.core.setup.Bootstrap},
 * {@link io.dropwizard.core.Configuration}, {@link Environment},
 * {@link ru.vyarus.dropwizard.guice.module.yaml.ConfigurationTree}, {@link com.google.inject.Injector},
 * {@link ru.vyarus.dropwizard.guice.module.context.option.Options} (see shortcut instance methods).
 * <p>
 * To debug shared state access use {@link #getAccessReport()}
 * (or {@link ru.vyarus.dropwizard.guice.GuiceBundle.Builder#printSharedStateUsage()}).
 *
 * @author Vyacheslav Rusakov
 * @since 26.09.2019
 */
@SuppressWarnings("rawtypes")
public class SharedConfigurationState {
    /**
     * Attribute name used to store application instance in application context attributes.
     */
    public static final String CONTEXT_APPLICATION_PROPERTY = "guicey.context.application";

    private static final Map<Application, SharedConfigurationState> STATE = Maps.newConcurrentMap();

    /**
     * During application startup all initialization performed in the single thread, and so it is possible
     * to reference shared state instance with a simple static call. This is required because in some cases,
     * neither Application nor Environment objects are accessible (e.g., BindingInstaller, BundlesLookup).
     */
    private static final ThreadLocal<SharedConfigurationState> STARTUP_INSTANCE = new ThreadLocal<>();

    // string used as key to workaround potential problems with different class loaders
    private final Map<String, Object> state = new LinkedHashMap<>();

    // put calls
    private final Map<String, String> statePopulationTrack = new LinkedHashMap<>();
    // get calls (including misses)
    private final Multimap<String, String> stateAccessTrack = LinkedHashMultimap.create();
    // listener registration (to show state delayed access correctly)
    private final Map<Consumer, String> listenersTrack = new LinkedHashMap<>();
    // reactive values
    // NOTE: no validation for not called listeners to allow optional reactive states
    private final Multimap<String, Consumer> listeners = LinkedHashMultimap.create();
    private Application application;

    public SharedConfigurationState() {
        // make shared state accessible during startup in all places
        STARTUP_INSTANCE.set(this);
    }

    /**
     * Note: although class is used for key, actual value is stored with full class name string.
     * So classes, loaded from different class loaders will lead to the same value.
     *
     * @param key shared object key
     * @param <V> shared object type
     * @return value or null
     */
    public <V> V get(final Class<V> key) {
        return get(key.getName());
    }

    /**
     * Special string-based state access for revising state with {@link #getKeys()}.
     *
     * @param key state key
     * @param <V> value type
     * @return state value or null
     */
    @SuppressWarnings("unchecked")
    public <V> V get(final String key) {
        final V v = (V) state.get(key);
        stateAccessTrack.put(key, (v == null ? "MISS " : "GET  ") + StackUtils.getSharedStateSource());
        return v;
    }

    /**
     * Get value or declare default if value not set yet.
     *
     * @param key          shared object key
     * @param defaultValue default shared object provider
     * @param <V>          shared object type
     * @return stored or default (just stored) value
     */
    public <V> V get(final Class<V> key, final Supplier<V> defaultValue) {
        if (!state.containsKey(key.getName())) {
            put(key, defaultValue.get());
        }
        return get(key);
    }

    /**
     * Shortcut for {@link #get(Class)} to immediately fail if value not set. Supposed to be used by shared state
     * consumers (to validate situations when value must exist for sure).
     *
     * @param key     shared object key
     * @param message exception message (could use {@link String#format(String, Object...)} placeholders)
     * @param args    placeholder arguments for error message
     * @param <V>     shared object type
     * @return stored object (never null)
     * @throws IllegalStateException if value isn't set
     */
    public <V> V getOrFail(final Class<V> key, final String message, final Object... args) {
        final V res = get(key);
        if (res == null) {
            throw new IllegalStateException(Strings.lenientFormat(message, args));
        }
        return res;
    }

    /**
     * Reactive shared value access: if value already available action called immediately, otherwise action would
     * be called when value set (note that value could be set only once).
     * <p>
     * Note: listener would not be called if the state is never set. This assumed to be used for optional state cases.
     *
     * @param key    shared object key
     * @param action action to execute when value would be set
     * @param <V>    value type
     */
    public <V> void whenReady(final Class<V> key, final Consumer<V> action) {
        final String name = key.getName();
        if (state.containsKey(name)) {
            action.accept(get(name));
        } else {
            listeners.put(name, action);
            listenersTrack.put(action, StackUtils.getSharedStateSource());
        }
    }

    // ---- providers for common objects

    /**
     * Options access object is always available.
     *
     * @return options access object
     */
    public Options getOptions() {
        return Preconditions.checkNotNull(get(Options.class), "Options object not yet available");
    }

    /**
     * Bootstrap object is available since guice bundle initialization start. It will not be available for
     * hooks (because hooks processed before guice bundle initialization call - no way to get boostrap reference).
     *
     * @param <C> configuration type
     * @return bootstrap instance provider (would fail if called too early)
     */
    @SuppressWarnings("unchecked")
    public <C extends Configuration> Provider<Bootstrap<C>> getBootstrap() {
        return () -> Preconditions.checkNotNull(get(Bootstrap.class), "Bootstrap object not yet available");
    }

    /**
     * Application object is available since guice bundle initialization start.
     *
     * @param <C> configuration type
     * @return application instance provider (would fail if called too early)
     */
    @SuppressWarnings("unchecked")
    public <C extends Configuration> Provider<Application<C>> getApplication() {
        return () -> Optional.ofNullable(get(Bootstrap.class))
                .map(b -> (Application<C>) b.getApplication())
                .orElseThrow(() -> new NullPointerException("Application instance is not yet available"));
    }

    /**
     * Environment instance is available since guice bundle run.
     *
     * @return environment instance provider (would fail if called too early)
     */
    public Provider<Environment> getEnvironment() {
        return () -> Preconditions.checkNotNull(get(Environment.class),
                "Environment is not yet available");
    }

    /**
     * Configuration instance is available since guice bundle run.
     *
     * @param <C> configuration type
     * @return configuration instance provider (would fail if called too early)
     */
    @SuppressWarnings("unchecked")
    public <C extends Configuration> Provider<C> getConfiguration() {
        return () -> (C) Preconditions.checkNotNull(get(Configuration.class),
                "Configuration is not yet available");
    }

    /**
     * Configuration instance is available since guice bundle run.
     *
     * @return configuration tree instance provider (would fail if called too early)
     */
    public Provider<ConfigurationTree> getConfigurationTree() {
        return () -> Preconditions.checkNotNull(get(ConfigurationTree.class),
                "ConfigurationTree is not yet available");
    }

    /**
     * Injector instance is created under guice bundle run.
     *
     * @return configuration instance provider (would fail if called too early)
     * @see ru.vyarus.dropwizard.guice.injector.lookup.InjectorLookup for simpler lookup method
     */
    public Provider<Injector> getInjector() {
        return () -> Preconditions.checkNotNull(get(Injector.class),
                "Injector is not yet available");
    }

    // ---- end of common object providers

    /**
     * Assumed to be used to store some configuration during startup. For example, if multiple bundle instances
     * should know of each other - they could use shared state to communicate.
     * <p>
     * Class is used for key to avoid dummy mistakes with strings (in most cases bundle class would be key).
     * But internally string class name is used in order to unify classes from different class loaders.
     * <p>
     * Note: warning will be printed if existing state value is overridden because it is assumed that
     * some global configuration object would be shared once and later all participants will work with the same object.
     *
     * @param key   shared object key
     * @param value shared value (usually configuration object)
     * @param <V>   value type
     */
    @SuppressWarnings("unchecked")
    public <V> void put(final Class<V> key, final V value) {
        Preconditions.checkArgument(key != null, "Shared state key can't be null");
        // just to avoid dummy mistakes
        Preconditions.checkArgument(value != null, "Shared state does not accept null values");
        final String name = key.getName();
        Preconditions.checkState(!state.containsKey(name), "Shared state for key %s already defined", name);
        state.put(name, value);
        statePopulationTrack.put(name, StackUtils.getSharedStateSource());
        // processed one time
        listeners.removeAll(name).forEach(consumer -> {
            consumer.accept(value);
            stateAccessTrack.put(name, "GET " + listenersTrack.get(consumer));
        });
    }

    /**
     * Note: each key is a full name of registration class.
     *
     * @return all available keys
     */
    public Set<String> getKeys() {
        return new HashSet<>(state.keySet());
    }

    /**
     * Clear state for exact application. Normally, state should shut down automatically (as it is a managed object).
     * But in tests with disabled lifecycle this will not happen and so cleanup must be manual.
     *
     * @see SharedConfigurationState#clear() to remove all states (for tests)
     */
    @VisibleForTesting
    public void shutdown() {
        STATE.remove(application);
    }

    @Override
    public String toString() {
        return "Shared state with " + state.size() + " objects: " + String.join(", ", getKeys());
    }

    /**
     * Called on initialization phase to assign registry instance to application (to be able to statically
     * reference registry).
     *
     * @param application application instance
     */
    protected void assignTo(final Application application) {
        this.application = application;
        Preconditions.checkState(!STATE.containsKey(application),
                "Shared state already associated with application %s", application.getClass().getName());
        STATE.put(application, this);
    }

    /**
     * Called on run phase to assign to application lifecycle and listen for shutdown.
     *
     * @param environment environment object
     */
    protected void listen(final Environment environment) {
        // storing application reference in context attributes (to be able to reference shared state by environment)
        environment.getApplicationContext().setAttribute(CONTEXT_APPLICATION_PROPERTY, application);
        environment.lifecycle().manage(new RegistryShutdown(this));
    }

    /**
     * Called after application startup to cleanup thread local instance. After this moment call to
     * {@link #getStartupInstance()} would lead to exception (since that moment it is not a problem to obtain
     * application or environment instances and resolve state with them).
     */
    protected void forgetStartupInstance() {
        STARTUP_INSTANCE.remove();
    }

    /**
     * During application startup it is not always possible to lookup configuration state with
     * {@link Application} or {@link Environment} objects. For such cases simplified static call can be used.
     * For example, in binding installer call, bundles lookup implementation, hook.
     * <p>
     * IMPORTANT: It will work only during application startup and only from main application thread! Would not be
     * available in application run method (becuase at this point guice bundle is already processed).
     *
     * @return shared configuration state instance
     * @throws java.lang.NullPointerException if called after startup, from non-main thread or before guice bundle
     *                                        registration (too early).
     */
    public static SharedConfigurationState getStartupInstance() {
        return Preconditions.checkNotNull(STARTUP_INSTANCE.get(),
                "Shared state startup instance is not available: either you try to access it from different thread"
                        + "or trying to obtain instance after application startup (in later case, use lookup by "
                        + "application or environment objects instead).");
    }

    /**
     * Static lookup for registry value.
     *
     * @param application application instance
     * @param key         shared object key
     * @param <V>         shared object key
     * @return value optional
     */
    public static <V> Optional<V> lookup(final Application application, final Class<V> key) {
        return get(application).map(value -> value.get(key));
    }

    /**
     * Static lookup for registry value by environment instance.
     *
     * @param environment dropwizard environment object
     * @param key         shared object key
     * @param <V>         shared object key
     * @return value optional
     */
    public static <V> Optional<V> lookup(final Environment environment, final Class<V> key) {
        return get(environment).map(value -> value.get(key));
    }

    /**
     * Shortcut for {@link #lookup(Application, Class)} to immediately fail if value is not available.
     *
     * @param application application instance
     * @param key         shared object key
     * @param message     exception message (could use {@link String#format(String, Object...)} placeholders)
     * @param args        placeholder arguments for error message
     * @param <V>         shared object type
     * @return value (never null)
     * @throws IllegalStateException if value not available
     */
    public static <V> V lookupOrFail(final Application application,
                                     final Class<V> key,
                                     final String message,
                                     final Object... args) {
        return lookup(application, key)
                .orElseThrow(() -> new IllegalStateException(Strings.lenientFormat(message, args)));
    }

    /**
     * Shortcut for {@link #lookup(Environment, Class)} to immediately fail if value is not available.
     *
     * @param environment environment instance
     * @param key         shared object key
     * @param message     exception message (could use {@link String#format(String, Object...)} placeholders)
     * @param args        placeholder arguments for error message
     * @param <V>         shared object type
     * @return value (never null)
     * @throws IllegalStateException if value not available
     */
    public static <V> V lookupOrFail(final Environment environment,
                                     final Class<V> key,
                                     final String message,
                                     final Object... args) {
        return lookup(environment, key)
                .orElseThrow(() -> new IllegalStateException(Strings.lenientFormat(message, args)));
    }

    /**
     * Static lookup for entire application registry.
     *
     * @param application application instance
     * @return optional of application registry (may be empty if called too early or too late)
     */
    @SuppressWarnings("checkstyle:OverloadMethodsDeclarationOrder")
    public static Optional<SharedConfigurationState> get(final Application application) {
        return Optional.ofNullable(STATE.get(application));
    }

    /**
     * Static lookup for entire application registry by environment instance.
     *
     * @param environment environment instance
     * @return optional of application registry (may be empty if called too early or too late)
     */
    public static Optional<SharedConfigurationState> get(final Environment environment) {
        final Application application = (Application) environment.getApplicationContext()
                .getAttribute(CONTEXT_APPLICATION_PROPERTY);
        return application == null ? Optional.empty() : get(application);
    }

    /**
     * Shortcut for {@link #get(Application)} to immediately fail if registry is not available.
     *
     * @param application application instance
     * @param message     exception message (could use {@link String#format(String, Object...)} placeholders)
     * @param args        placeholder arguments for error message
     * @return registry (never null)
     * @throws IllegalStateException if no state is associated with application yet
     */
    @SuppressWarnings("checkstyle:OverloadMethodsDeclarationOrder")
    public static SharedConfigurationState getOrFail(final Application application,
                                                     final String message,
                                                     final Object... args) {
        return get(application)
                .orElseThrow(() -> new IllegalStateException(Strings.lenientFormat(message, args)));
    }

    /**
     * Shortcut for {@link #get(Environment)} to immediately fail if registry is not available.
     *
     * @param environment environment instance
     * @param message     exception message (could use {@link String#format(String, Object...)} placeholders)
     * @param args        placeholder arguments for error message
     * @return registry (never null)
     * @throws IllegalStateException if no state is associated with application yet
     */
    @SuppressWarnings("checkstyle:OverloadMethodsDeclarationOrder")
    public static SharedConfigurationState getOrFail(final Environment environment,
                                                     final String message,
                                                     final Object... args) {
        return get(environment)
                .orElseThrow(() -> new IllegalStateException(Strings.lenientFormat(message, args)));
    }

    /**
     * Clears stored injectors references. Normally shouldn't be used at all, because managed object will detect
     * application shutdown and remove assigned state (for example, when used DropwizardAppRule or GuiceyAppRule).
     * <p>
     * May be useful in tests, when application was not shut down properly (but just to clear memory).
     * <p>
     * WARNING: calling this method while application is working may cause incorrect behaviour.
     */
    @VisibleForTesting
    public static void clear() {
        STATE.clear();
    }

    /**
     * Could be useful only in tests in order to validate possibly stale applications.
     *
     * @return number of registered contexts
     */
    @VisibleForTesting
    public static int statesCount() {
        return STATE.size();
    }

    /**
     * Shows all objects in state with a link to assignment source and all state access attempts (success or misses).
     *
     * @return shared state usage report
     */
    public String getAccessReport() {
        final StringBuilder report = new StringBuilder(200);
        boolean blankLineAdded = false;
        for (Map.Entry<String, String> entry : statePopulationTrack.entrySet()) {
            final String k = entry.getKey();
            final String value = entry.getValue();
            final Collection<String> gets = stateAccessTrack.get(k);
            report.append(!blankLineAdded && (report.isEmpty() || !gets.isEmpty()) ? "\n" : "").append("\tSET ")
                    .append(String.format("%-80s\t %s%n", renderKey(k), value));
            blankLineAdded = false;
            gets.forEach(s -> report.append("\t\t").append(s).append('\n'));
            if (!gets.isEmpty()) {
                report.append('\n');
                blankLineAdded = true;
            }
        }

        final Set<String> notset = new LinkedHashSet<>(stateAccessTrack.keySet());
        notset.removeAll(statePopulationTrack.keySet());
        notset.forEach(key -> {
            report.append("\n\tNEVER SET ").append(renderKey(key)).append('\n');
            stateAccessTrack.get(key).forEach(s -> report.append("\t\t").append(s).append('\n'));
            // never called listeners
            listeners.get(key).forEach(consumer ->
                    report.append("\t\tMISS ").append(listenersTrack.get(consumer)).append('\n'));
        });

        // listeners-only missed access
        listeners.keySet().forEach(key -> {
            if (!notset.contains(key)) {
                report.append("\n\tNEVER SET ").append(renderKey(key)).append('\n');
                listeners.get(key).forEach(consumer ->
                        report.append("\t\tMISS ").append(listenersTrack.get(consumer)).append('\n'));
            }
        });

        return report.toString();
    }

    private String renderKey(final String key) {
        final int lastDot = key.lastIndexOf('.');
        return key.substring(lastDot + 1) + " (" + key.substring(0, lastDot) + ")";
    }

    /**
     * Remove global registration on shutdown. This is actually not important for real application.
     * Only could be sensible in tests when many application instances could be created (and not released without
     * this hook).
     */
    private static class RegistryShutdown implements Managed {
        private final SharedConfigurationState state;

        protected RegistryShutdown(final SharedConfigurationState state) {
            this.state = state;
        }

        @Override
        public void stop() throws Exception {
            state.shutdown();
        }
    }
}
