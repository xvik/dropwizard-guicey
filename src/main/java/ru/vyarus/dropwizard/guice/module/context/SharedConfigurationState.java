package ru.vyarus.dropwizard.guice.module.context;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import io.dropwizard.Application;
import io.dropwizard.lifecycle.Managed;
import io.dropwizard.setup.Environment;

import java.util.*;
import java.util.function.Supplier;

/**
 * Application-wide shared state assumed to be used in configuration phase. For example,
 * in complex cases when bundles must share some global state (otherwise it would require to maintain
 * some {@link ThreadLocal} field in bundle). But other cases could arise too. Intended to be used for very rare cases:
 * use it only if you can't avoid shared state in configuration time (for example, like guicey gsp bundle,
 * which requires global configuration, accessible by multiple bundles).
 * <p>
 * Internally, guicey use it to store created {@link com.google.inject.Injector} and make it available statically
 * (see {@link ru.vyarus.dropwizard.guice.injector.lookup.InjectorLookup}).
 * <p>
 * Universal sharing place should simplify testing in the future: if new global state will appear, there would be no
 * need to reset anything in tests (to clear state, for example, on testing errors). One "dirty" place to replace
 * all separate hacks.
 * <p>
 * Shared state could be accessed statically with {@link #get(Application)}, or within
 * {@link ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBundle}. Guicey hooks could use
 * {@link ru.vyarus.dropwizard.guice.GuiceBundle.Builder#withSharedState(java.util.function.Consumer)} to
 * access application state.
 * <p>
 * Classes are used as state keys to simplify usage (in most cases, bundle class will be used as key).
 * Shared value could be set only once (to prevent complex situations with state substitutions). It is advised
 * to initialize shared value only in initialization phase (to avoid potential static access errors).
 *
 * @author Vyacheslav Rusakov
 * @since 26.09.2019
 */
public class SharedConfigurationState {
    private static final Map<Application, SharedConfigurationState> STATE = Maps.newConcurrentMap();

    private final Map<String, Object> state = new HashMap<>();
    private Application application;

    /**
     * Note: in spite of fact that class is used for key, actual value is stored with full class name string.
     * So classes, loaded from different class loaders will lead to the same value.
     *
     * @param key shared object key
     * @param <V> shared object type
     * @return value or null
     */
    @SuppressWarnings("unchecked")
    public <V> V get(final Class<?> key) {
        return (V) state.get(key.getName());
    }

    /**
     * Get value or declare default if value not set yet.
     *
     * @param key          shared object key
     * @param defaultValue default shared object provider
     * @param <V>          shared object type
     * @return stored or default (just stored) value
     */
    public <V> V get(final Class<?> key, final Supplier<V> defaultValue) {
        V res = get(key);
        if (res == null && defaultValue != null) {
            res = defaultValue.get();
            put(key, res);
        }
        return res;
    }

    /**
     * Shortcut for {@link #get(Class)} to immediately fail if value not set. Supposed to be used by shared state
     * consumers (to validate situations when value must exists for sure).
     *
     * @param key     shared object key
     * @param message exception message (could use {@link String#format(String, Object...)} placeholders)
     * @param args    placeholder arguments for error message
     * @param <V>     shared object type
     * @return stored object (never null)
     * @throws IllegalStateException if value not set
     */
    public <V> V getOrFail(final Class<?> key, final String message, final Object... args) {
        final V res = get(key);
        if (res == null) {
            throw new IllegalStateException(Strings.lenientFormat(message, args));
        }
        return res;
    }

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
     */
    public void put(final Class<?> key, final Object value) {
        Preconditions.checkArgument(key != null, "Registry key can't be null");
        // just to avoid dummy mistakes
        Preconditions.checkArgument(value != null, "Registry does not accept null values");
        final String name = key.getName();
        Preconditions.checkState(!state.containsKey(name), "Global state for key %s already defined", name);
        state.put(name, value);
    }

    /**
     * Note: each key is a full name of registration class.
     *
     * @return all available keys
     */
    public Set<String> getKeys() {
        return new HashSet<>(state.keySet());
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
                "Global state already associated with application %s", application.getClass().getName());
        STATE.put(application, this);
    }

    /**
     * Called on run phase to assign to application lifecycle and listen for shutdown.
     *
     * @param environment environment  object
     */
    protected void listen(final Environment environment) {
        environment.lifecycle().manage(new RegistryShutdown(application));
    }

    /**
     * Static lookup for registry value.
     *
     * @param application application instance
     * @param key         shared object key
     * @param <V>         shared object type
     * @return value optional
     */
    public static <V> Optional<V> lookup(final Application application, final Class<?> key) {
        final Optional<SharedConfigurationState> state = get(application);
        return state.map(value -> value.get(key));
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
    @SuppressWarnings("unchecked")
    public static <V> V lookupOrFail(final Application application,
                                     final Class<?> key,
                                     final String message,
                                     final Object... args) {
        return ((Optional<V>) lookup(application, key))
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
     * Remove global registration on shutdown. This is actually not important for real application.
     * Only could be sensible in tests when many application instances could be created (and not released without
     * this hook).
     */
    private static class RegistryShutdown implements Managed {
        private final Application application;

        protected RegistryShutdown(final Application application) {
            this.application = application;
        }

        @Override
        public void start() throws Exception {
            // not used
        }

        @Override
        public void stop() throws Exception {
            STATE.remove(application);
        }
    }
}
