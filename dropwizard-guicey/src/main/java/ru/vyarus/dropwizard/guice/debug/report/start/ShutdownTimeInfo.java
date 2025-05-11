package ru.vyarus.dropwizard.guice.debug.report.start;

import ru.vyarus.dropwizard.guice.module.context.stat.StatsInfo;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Shutdown time aggregation object (for startup report).
 *
 * @author Vyacheslav Rusakov
 * @since 10.03.2025
 */
public class ShutdownTimeInfo {

    private Duration stopTime;
    private final Map<Class, Duration> managedTimes = new LinkedHashMap<>();
    // managed or lifecycle
    private final Map<Class, String> managedTypes = new LinkedHashMap<>();
    private Duration listenersTime;
    private final List<Class> events = new ArrayList<>();
    private StatsInfo stats;

    /**
     * @return shutdown time
     */
    public Duration getStopTime() {
        return stopTime;
    }

    /**
     * @return managed objects durations
     */
    public Map<Class, Duration> getManagedTimes() {
        return managedTimes;
    }

    /**
     * @return types of managed objects
     */
    public Map<Class, String> getManagedTypes() {
        return managedTypes;
    }

    /**
     * @return overall listeners time
     */
    public Duration getListenersTime() {
        return listenersTime;
    }

    /**
     * @param listenersTime overall listeners time
     */
    public void setListenersTime(final Duration listenersTime) {
        this.listenersTime = listenersTime;
    }

    /**
     * @param stopTime shutdown time
     */
    public void setStopTime(final Duration stopTime) {
        this.stopTime = stopTime;
    }

    /**
     * @return executed guicey events
     */
    public List<Class> getEvents() {
        return events;
    }

    /**
     * @return stats instance
     */
    public StatsInfo getStats() {
        return stats;
    }

    /**
     * @param stats stats instance
     */
    public void setStats(final StatsInfo stats) {
        this.stats = stats;
    }
}
