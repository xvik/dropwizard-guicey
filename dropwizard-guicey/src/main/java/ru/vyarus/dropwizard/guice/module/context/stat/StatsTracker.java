package ru.vyarus.dropwizard.guice.module.context.stat;

import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Maps;

import java.time.Duration;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static ru.vyarus.dropwizard.guice.module.context.stat.Stat.GuiceyTime;
import static ru.vyarus.dropwizard.guice.module.context.stat.Stat.JerseyTime;
import static ru.vyarus.dropwizard.guice.module.context.stat.Stat.OverallTime;

/**
 * Internal object, used to record startup stats. Guava {#Stopwatch} used for time measurements
 * (also native stopwatch time formatting is used).
 * All metrics are cumulative (all measurements are summed).
 *
 * @author Vyacheslav Rusakov
 * @since 27.07.2016
 */
public final class StatsTracker {
    private final Map<Stat, StatTimer> timers = Maps.newEnumMap(Stat.class);
    private final Map<Stat, Integer> counters = Maps.newEnumMap(Stat.class);
    private final GuiceStatsTracker guiceStats = new GuiceStatsTracker();
    private final Map<DetailStat, Map<Class<?>, Stopwatch>> detailStats = Maps.newEnumMap(DetailStat.class);

    public StatsTracker() {
        // start measuring overall time since guice bundle creation
        timer(OverallTime);
    }

    /**
     * If measured first time, returns new instance. For second and following measures returns the same instance
     * (to sum measurements).
     * Assumed proper usage: timer stat provided and returned watch correctly stopped.
     *
     * @param name statistic name
     * @return timer to measure time
     */
    public StatTimer timer(final Stat name) {
        final StatTimer watch = timers.computeIfAbsent(name, k -> new StatTimer(Stopwatch.createUnstarted()));
        // if time was measured before then new time will sum with current (if timer already started then current
        // start would be ignored)
        watch.start();
        return watch;
    }

    /**
     * Stop running timer.
     *
     * @param name timer name
     */
    public void stopTimer(final Stat name) {
        Preconditions.checkNotNull(timers.get(name), "No started timer for " + name).stop();
    }

    /**
     * Detail stats used to record exact entity metric (like guicey bundle init or run time).
     *
     * @param name detail name
     * @param type target type
     * @return detail timer
     */
    public Stopwatch detailTimer(final DetailStat name, final Class<?> type) {
        return detailStats.computeIfAbsent(name, detailStat -> new LinkedHashMap<>())
                .computeIfAbsent(type, aClass -> Stopwatch.createUnstarted()).start();
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
     * Special methods for tracking time in jersey scope.
     * Such complication used to avoid using 3 different trackers in code.
     * Jersey initialization is performed after bundles run and so out of scope of GuiceBundle.
     *
     * @param name jersey statistics name
     */
    public void startJerseyTimer(final Stat name) {
        timer(GuiceyTime);
        if (!JerseyTime.equals(name)) {
            timer(JerseyTime);
        }
        timer(name);
    }

    /**
     * Called to stop currently measured jersey metric (also stops main guice and jersey timers).
     *
     * @param name jersey statistic name
     */
    public void stopJerseyTimer(final Stat name) {
        timers.get(GuiceyTime).stop();
        if (!JerseyTime.equals(name)) {
            timers.get(JerseyTime).stop();
        }
        timers.get(name).stop();
    }

    /**
     * @return collected timers map
     */
    public Map<Stat, Stopwatch> getTimers() {
        return timers.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getStopwatch()));
    }

    /**
     * @param name detail stat name
     * @return detail stats
     */
    public Map<Class<?>, Duration> getDetails(final DetailStat name) {
        final Map<Class<?>, Stopwatch> details = detailStats.get(name);
        if (details == null) {
            return Collections.emptyMap();
        }
        final Map<Class<?>, Duration> res = new LinkedHashMap<>();
        details.forEach((type, stopwatch) -> res.put(type, stopwatch.elapsed()));
        return res;
    }

    /**
     * @return collected counters map
     */
    public Map<Stat, Integer> getCounters() {
        return counters;
    }

    /**
     * @return guice stats logger tracker object
     */
    public GuiceStatsTracker getGuiceStats() {
        return guiceStats;
    }

    /**
     * Verify all timers stopped on application complete startup. As timers are inlinable, it is quite possible
     * to not call stop enough times.
     */
    public void verifyTimersDone() {
        timers.get(OverallTime).stop();
        for (final Map.Entry<Stat, Stopwatch> entry : getTimers().entrySet()) {
            Preconditions.checkState(!entry.getValue().isRunning(),
                    "Timer is still running after application startup", entry.getKey());
        }
    }
}
