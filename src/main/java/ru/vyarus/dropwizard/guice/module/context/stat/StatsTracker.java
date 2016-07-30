package ru.vyarus.dropwizard.guice.module.context.stat;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Maps;

import java.util.Map;

import static ru.vyarus.dropwizard.guice.module.context.stat.Stat.GuiceyTime;
import static ru.vyarus.dropwizard.guice.module.context.stat.Stat.HKTime;

/**
 * Internal object, used to record startup stats. Guava {#Stopwatch} used for time measurements
 * (also native stopwatch time formatting is used).
 * All metrics are cumulative (all measurements are summed).
 *
 * @author Vyacheslav Rusakov
 * @since 27.07.2016
 */
public final class StatsTracker {
    private final Map<Stat, Stopwatch> timers = Maps.newEnumMap(Stat.class);
    private final Map<Stat, Integer> counters = Maps.newEnumMap(Stat.class);

    /**
     * If measured first time, returns new instance. For second and following measures returns the same instance
     * (to sum measurements).
     * Assumed proper usage: timer stat provided and returned watch correctly stopped.
     *
     * @param name statistic name
     * @return timer to measure time
     */
    public Stopwatch timer(final Stat name) {
        Stopwatch watch = timers.get(name);
        if (watch == null) {
            watch = Stopwatch.createUnstarted();
            timers.put(name, watch);
        }
        // if watch was performed before then new time will sum with current
        watch.start();
        return watch;
    }

    /**
     * Inserts value for first call and sum values for consequent calls.
     *
     * @param name  statistics name
     * @param count counter value
     */
    public void count(final Stat name, final int count) {
        Integer value = counters.get(name);
        value = value == null ? count : value + count;
        counters.put(name, value);
    }

    /**
     * Special methods for tracking time in hk scope.
     * Such complication used to avoid using 3 different trackers in code.
     * Hk initialization is performed after bundles run and so out of scope of GuiceBundle.
     *
     * @param name hk statistics name
     */
    public void startHkTimer(final Stat name) {
        timer(GuiceyTime);
        if (!HKTime.equals(name)) {
            timer(HKTime);
        }
        timer(name);
    }

    /**
     * Called to stop currently measured hk metric (also stops main guice and hk timers).
     *
     * @param name hk statistic name
     */
    public void stopHkTimer(final Stat name) {
        timers.get(GuiceyTime).stop();
        if (!HKTime.equals(name)) {
            timers.get(HKTime).stop();
        }
        timers.get(name).stop();
    }

    /**
     * @return collected timers map
     */
    public Map<Stat, Stopwatch> getTimers() {
        return timers;
    }

    /**
     * @return collected counters map
     */
    public Map<Stat, Integer> getCounters() {
        return counters;
    }
}
