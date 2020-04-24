package ru.vyarus.dropwizard.guice.module.context.stat;

import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Provides access to starts collected at startup.
 * Instance bound to guice context and available for injection.
 * Prefer using through {@link ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo#getStats()}.
 *
 * @author Vyacheslav Rusakov
 * @since 28.07.2016
 */
public final class StatsInfo {

    // have to keep full object, because stats also computed after info object creation
    private final StatsTracker tracker;

    public StatsInfo(final StatsTracker tracker) {
        this.tracker = tracker;
    }

    /**
     * Note: time stats are measured in nanoseconds and after conversion to millisecond it may become 0.
     * Usually it makes no sense to log such small values, and 0 value makes it easier to filter out them.
     * In contrast, when using {@link #humanTime(Stat)} for such timers, correct value will be printed.
     *
     * @param name statistic name
     * @return collected time in milliseconds or 0 (is stat value is not available)
     * @throws IllegalStateException if provided stat is not time stat
     */
    public long time(final Stat name) {
        name.requiresTimer();
        final Stopwatch stopwatch = tracker.getTimers().get(name);
        return stopwatch == null ? 0 : stopwatch.elapsed(TimeUnit.MILLISECONDS);
    }

    /**
     * Value is reported in best suited units (e.g. milliseconds, seconds, minutes etc).
     *
     * @param name statistic name
     * @return human readable (formatted) timer value or 0 (if stat value is not available)
     * @throws IllegalStateException if provided stat is not time stat
     */
    public String humanTime(final Stat name) {
        name.requiresTimer();
        Preconditions.checkState(name.isTimer(), "Stat %s is not timer stat", name);
        final Stopwatch stopwatch = tracker.getTimers().get(name);
        return stopwatch == null ? "0" : stopwatch.toString();
    }

    /**
     * @param name statistic name
     * @return stat value or 0 (if stat value is not available)
     * @throws IllegalStateException if provided stat is not count stat
     */
    public int count(final Stat name) {
        name.requiresCounter();
        final Integer value = tracker.getCounters().get(name);
        return value == null ? 0 : value;
    }

    /**
     * @return guice injector creation logs (intercepted)
     */
    public List<String> getGuiceStats() {
        return tracker.getGuiceStats().getMessages();
    }
}
