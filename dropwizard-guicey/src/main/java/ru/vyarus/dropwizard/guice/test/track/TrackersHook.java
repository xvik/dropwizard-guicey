package ru.vyarus.dropwizard.guice.test.track;

import com.codahale.metrics.MetricRegistry;
import com.google.common.base.Preconditions;
import com.google.inject.matcher.Matchers;
import ru.vyarus.dropwizard.guice.GuiceBundle;
import ru.vyarus.dropwizard.guice.hook.GuiceyConfigurationHook;

import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

/**
 * Track method calls on any guice bean and records arguments and return values, together with measuring time.
 * <p>
 * Useful for validating called method arguments and return value (when service called indirectly - by another top-level
 * service). In this sense it is very close to mockito spy
 * ({@link ru.vyarus.dropwizard.guice.test.spy.SpiesHook}), but api is simpler.
 * Tracker collects both raw in/out objects and string (snapshot) version (because mutable objects could change).
 * Raw objects holding could be disabled ({@link TrackersHook.Builder#keepRawObjects(boolean)}).
 * <p>
 * Another use-case is slow methods detection: tracker counts each method execution time, and after test could
 * print a report indicating the slowest methods. Or it could be used to simply print all called methods to console
 * with {@link TrackersHook.Builder#trace(boolean)} (could be useful during behavior investigations). Another option
 * is to configure a slow method threshold: then only methods above a threshold would be logged with WARN.
 * <p>
 * Example usage:
 * <pre><code>
 *     TrackersHook hook = new TrackersHook()
 *     Tracker&lt;Service&gt; tracker = hook.track(Service.class)
 *                                          // optional configuration
 *                                          .add()
 *     // after service methods execution
 *     tracker.getTracks()
 * </code></pre>
 * <p>
 * Tracking is implemented with a custom AOP handler which intercepts all bean calls and record them.
 * Can be used together with mocks, spies or stubs
 * <p>
 * Limitation: could track only beans, created by guice (due to used AOP). Does not work for HK2 beans.
 *
 * @author Vyacheslav Rusakov
 * @since 28.04.2025
 */
public class TrackersHook implements GuiceyConfigurationHook {

    // independent from dropwizard app registry
    private final MetricRegistry metrics = new MetricRegistry();
    private final Map<Class<?>, TrackerProxy<?>> trackers = new HashMap<>();
    private boolean initialized;

    @Override
    public void configure(final GuiceBundle.Builder builder) throws Exception {
        if (!trackers.isEmpty()) {
            builder.modulesOverride(binder -> {
                trackers.forEach((type, proxy) -> {
                    // real binding isn't overridden, just used aop to intercept all calls
                    binder.bindInterceptor(Matchers.only(type), Matchers.any(), proxy);
                });
            });
        }
        initialized = true;
    }

    /**
     * Start bean tracker registration.
     *
     * @param type bean type
     * @param <T>  bean type
     * @return builder to configure tracker
     * @throws java.lang.IllegalStateException if tracker for bean already registered
     */
    public <T> Builder<T> track(final Class<T> type) {
        Preconditions.checkState(!initialized, "Too late. Trackers already applied.");
        Preconditions.checkState(!trackers.containsKey(type), "Tracker object for type %s is already registered.",
                type.getSimpleName());
        return new Builder<>(type);
    }

    /**
     * @param type bean type
     * @param <T>  bean type
     * @return bean tracker instance
     * @throws java.lang.IllegalStateException if tracker for bean is not registered
     */
    @SuppressWarnings("unchecked")
    public <T> Tracker<T> getTracker(final Class<T> type) {
        return (Tracker<T>) Preconditions.checkNotNull(trackers.get(type),
                "Tracker not registered for type %s", type.getSimpleName()).getTracker();
    }

    /**
     * Clear recorded data for all trackers.
     */
    public void resetTrackers() {
        trackers.values().forEach(proxy -> proxy.getTracker().clear());
    }

    /**
     * Tracker configuration builder.
     *
     * @param <T> bean type
     */
    public class Builder<T> {
        private final Class<T> type;
        private final TrackerConfig config = new TrackerConfig();

        /**
         * Create builder.
         *
         * @param type service type
         */
        public Builder(final Class<T> type) {
            this.type = type;
        }

        /**
         * When enabled, prints called method just after it's execution (with called arguments and returned result).
         * Not enabled by default to avoid output mess in case when many methods would be called during test.
         *
         * @param trace true to print each method execution
         * @return builder instance for chained calls
         */
        public Builder<T> trace(final boolean trace) {
            config.setTrace(trace);
            return this;
        }

        /**
         * Print warnings about methods executing longer than the specified threshold.
         *
         * @param maxTime slow method threshold
         * @param unit    threshold unit
         * @return builder instance for chained calls
         * @see #disableSlowMethodsLogging() for disabling slow time logging
         */
        public Builder<T> slowMethods(final long maxTime, final ChronoUnit unit) {
            config.setSlowMethods(maxTime);
            config.setSlowMethodsUnit(unit);
            return this;
        }

        /**
         * Disable slow methods warning (by default, showing methods executed longer than 5 seconds).
         *
         * @return builder instance for chained calls
         * @see #slowMethods(long, java.time.temporal.ChronoUnit) for changing the default threshold
         */
        public Builder<T> disableSlowMethodsLogging() {
            config.setSlowMethods(0);
            return this;
        }

        /**
         * It is more likely that trackers would be used mostly for "call and verify" scenarios where keeping raw
         * arguments makes perfect sense. That's why it's enabled by default.
         * <p>
         * Important: method arguments and the result objects state could be mutable and change after or during method
         * execution (and so be irrelevant for tracks analysis). For such cases, the tracker always holds string
         * representations of method arguments and the result (rendered in method execution time).
         * <p>
         * It makes sense to disable option if too many method executions appear during the test (e.g., tracker used
         * for performance metrics).
         *
         * @param keepRawObjects true to keep raw arguments and result objects
         * @return builder instance for chained calls
         */
        public Builder<T> keepRawObjects(final boolean keepRawObjects) {
            config.setKeepRawObjects(keepRawObjects);
            return this;
        }

        /**
         * Required to keep called method toString rendering readable in case of large strings used.
         * Note that for non-string objects, an object type with identity hash would be shown (not rely on toString
         * because it would be too much unpredictable).
         *
         * @param maxStringLength maximum length of string in method parameter or returned result
         * @return builder instance for chained calls
         */
        public Builder<T> maxStringLength(final int maxStringLength) {
            config.setMaxStringLength(maxStringLength);
            return this;
        }

        /**
         * Apply tracker registration. Returned object should be used to access recorded tracks.
         *
         * @return configured and registered tracker
         */
        public Tracker<T> add() {
            final TrackerProxy<T> proxy = new TrackerProxy<>(type, config, metrics);
            trackers.put(type, proxy);
            return proxy.getTracker();
        }
    }
}
