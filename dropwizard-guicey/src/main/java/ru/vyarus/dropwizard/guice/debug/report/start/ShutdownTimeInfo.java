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

    public Duration getStopTime() {
        return stopTime;
    }

    public Map<Class, Duration> getManagedTimes() {
        return managedTimes;
    }

    public Map<Class, String> getManagedTypes() {
        return managedTypes;
    }

    public Duration getListenersTime() {
        return listenersTime;
    }

    public void setListenersTime(final Duration listenersTime) {
        this.listenersTime = listenersTime;
    }

    public void setStopTime(final Duration stopTime) {
        this.stopTime = stopTime;
    }

    public List<Class> getEvents() {
        return events;
    }

    public StatsInfo getStats() {
        return stats;
    }

    public void setStats(final StatsInfo stats) {
        this.stats = stats;
    }
}
