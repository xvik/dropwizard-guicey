package ru.vyarus.dropwizard.guice.debug.report.start;

import java.time.Duration;
import java.util.LinkedHashMap;
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

    public Duration getStopTime() {
        return stopTime;
    }

    public Map<Class, Duration> getManagedTimes() {
        return managedTimes;
    }

    public Map<Class, String> getManagedTypes() {
        return managedTypes;
    }

    public void setStopTime(final Duration stopTime) {
        this.stopTime = stopTime;
    }
}
