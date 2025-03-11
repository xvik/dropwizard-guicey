package ru.vyarus.dropwizard.guice.debug.report.start;

import org.eclipse.jetty.util.Uptime;
import ru.vyarus.dropwizard.guice.module.context.stat.StatsInfo;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Startup time aggregation object (for startup report). There are 3 main phases: init - everything from start till
 * last dw bundle init, run - dw bundles run (configuration and environment creation tracked separately as time
 * between bundles init and run), web - everything after bundles run until jersey lifecycle start (also includes
 * application run method).
 *
 * @author Vyacheslav Rusakov
 * @since 07.03.2025
 */
public class StartupTimeInfo {

    // jvm time before application start
    private final long jvmStart = Uptime.getUptime();

    // from guice bundle creation until last dropwizard bundle init (not include app init method)
    private Duration initTime;
    // last dropwizard bundle run finished (time since app start); does not include app run method)
    private Duration runPoint;
    // exclusive web lifecycle start time
    private Duration webTime;

    // time since start for each dropwizard bundle (can't be counted exclusively)
    private final Map<String, Duration> initPoints = new LinkedHashMap<>();
    // exclusive run time for each bundle
    private final Map<String, Duration> runTimes = new LinkedHashMap<>();

    private Duration initListenersTime;
    private Duration runListenersTime;

    private Duration initInstallersTime;
    private Duration initExtensionsTime;
    // configuration and environment creation time
    private Duration dwPreRunTime;

    // time between lifecycle starting and start
    private Duration lifecycleTime;
    // have to separate - otherwise can't differentiate them
    private final Map<Class, Duration> managedTimes = new LinkedHashMap<>();
    // managed or lifecycle
    private final Map<Class, String> managedTypes = new LinkedHashMap<>();

    private StatsInfo stats;

    public long getJvmStart() {
        return jvmStart;
    }

    public Duration getInitTime() {
        return initTime;
    }

    public void setInitTime(final Duration initTime) {
        this.initTime = initTime;
    }

    public Duration getRunPoint() {
        return runPoint;
    }

    public void setRunPoint(final Duration runPoint) {
        this.runPoint = runPoint;
    }

    public Duration getWebTime() {
        return webTime;
    }

    public void setWebTime(final Duration webTime) {
        this.webTime = webTime;
    }

    public Map<String, Duration> getInitPoints() {
        return initPoints;
    }

    public Map<String, Duration> getRunTimes() {
        return runTimes;
    }

    public Duration getInitListenersTime() {
        return initListenersTime;
    }

    public void setInitListenersTime(final Duration initListenersTime) {
        this.initListenersTime = initListenersTime;
    }

    public Duration getRunListenersTime() {
        return runListenersTime;
    }

    public void setRunListenersTime(final Duration runListenersTime) {
        this.runListenersTime = runListenersTime;
    }

    public Duration getInitExtensionsTime() {
        return initExtensionsTime;
    }

    public void setInitExtensionsTime(final Duration initExtensionsTime) {
        this.initExtensionsTime = initExtensionsTime;
    }

    public Duration getInitInstallersTime() {
        return initInstallersTime;
    }

    public void setInitInstallersTime(final Duration initInstallersTime) {
        this.initInstallersTime = initInstallersTime;
    }

    public Duration getDwPreRunTime() {
        return dwPreRunTime;
    }

    public void setDwPreRunTime(final Duration dwPreRunTime) {
        this.dwPreRunTime = dwPreRunTime;
    }

    public Duration getLifecycleTime() {
        return lifecycleTime;
    }

    public void setLifecycleTime(final Duration lifecycleTime) {
        this.lifecycleTime = lifecycleTime;
    }

    public Map<Class, Duration> getManagedTimes() {
        return managedTimes;
    }

    public Map<Class, String> getManagedTypes() {
        return managedTypes;
    }

    public StatsInfo getStats() {
        return stats;
    }

    public void setStats(final StatsInfo stats) {
        this.stats = stats;
    }
}
