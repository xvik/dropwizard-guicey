package ru.vyarus.dropwizard.guice.debug.report.start;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.eclipse.jetty.util.Uptime;
import ru.vyarus.dropwizard.guice.module.context.stat.StatsInfo;
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBundle;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
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
    private final Map<Class<?>, Duration> bundlesInitPoints = new LinkedHashMap<>();
    private final Multimap<Class<?>, Class<?>> guiceyBundleTransitives = ArrayListMultimap.create();
    private List<Class<? extends GuiceyBundle>> guiceyBundlesInitOrder;
    // exclusive run time for each bundle
    private final Map<Class<?>, Duration> bundlesRunTimes = new LinkedHashMap<>();

    private Duration initListenersTime;
    private Duration runListenersTime;

    private Duration initInstallersTime;
    private Duration initExtensionsTime;
    // configuration and environment creation time
    private Duration dwPreRunTime;

    // pure jersey time
    private Duration jerseyTime;
    // time between lifecycle starting and start
    private Duration lifecycleTime;
    // have to separate - otherwise can't differentiate them
    private final Map<Class, Duration> managedTimes = new LinkedHashMap<>();
    // managed or lifecycle
    private final Map<Class, String> managedTypes = new LinkedHashMap<>();

    private final List<Class> initEvents = new ArrayList<>();
    private final List<Class> runEvents = new ArrayList<>();
    private final List<Class> webEvents = new ArrayList<>();

    private StatsInfo stats;

    /**
     * @return jvm time before application
     */
    public long getJvmStart() {
        return jvmStart;
    }

    /**
     * @return from guice bundle creation until last dropwizard bundle init (not include app init method)
     */
    public Duration getInitTime() {
        return initTime;
    }

    /**
     * @param initTime overall init time
     */
    public void setInitTime(final Duration initTime) {
        this.initTime = initTime;
    }

    /**
     * @return guice bundles hierarchy
     */
    public Multimap<Class<?>, Class<?>> getGuiceyBundleTransitives() {
        return guiceyBundleTransitives;
    }

    /**
     * @return guicey bundles in initialization order
     */
    public List<Class<? extends GuiceyBundle>> getGuiceyBundlesInitOrder() {
        return guiceyBundlesInitOrder;
    }

    /**
     * @param guiceyBundlesInitOrder guicey bundles in initialization order
     */
    public void setGuiceyBundlesInitOrder(final List<Class<? extends GuiceyBundle>> guiceyBundlesInitOrder) {
        this.guiceyBundlesInitOrder = guiceyBundlesInitOrder;
    }

    /**
     * @return last dropwizard bundle run finished (time since app start); does not include app run method)
     */
    public Duration getRunPoint() {
        return runPoint;
    }

    /**
     * @param runPoint last dropwizard bundle run finished
     */
    public void setRunPoint(final Duration runPoint) {
        this.runPoint = runPoint;
    }

    /**
     * @return exclusive web lifecycle start time
     */
    public Duration getWebTime() {
        return webTime;
    }

    /**
     * @param webTime exclusive web lifecycle start time
     */
    public void setWebTime(final Duration webTime) {
        this.webTime = webTime;
    }

    /**
     * @return time since start for each dropwizard bundle (can't be counted exclusively)
     */
    public Map<Class<?>, Duration> getBundlesInitPoints() {
        return bundlesInitPoints;
    }

    /**
     * @return bundles run times
     */
    public Map<Class<?>, Duration> getBundlesRunTimes() {
        return bundlesRunTimes;
    }

    /**
     * @return overall listeners time during initialization
     */
    public Duration getInitListenersTime() {
        return initListenersTime;
    }

    /**
     * @param initListenersTime overall listeners time during initialization
     */
    public void setInitListenersTime(final Duration initListenersTime) {
        this.initListenersTime = initListenersTime;
    }

    /**
     * @return overall listeners time during run
     */
    public Duration getRunListenersTime() {
        return runListenersTime;
    }

    /**
     * @param runListenersTime overall listeners time during run
     */
    public void setRunListenersTime(final Duration runListenersTime) {
        this.runListenersTime = runListenersTime;
    }

    /**
     * @return overall extensions init time
     */
    public Duration getInitExtensionsTime() {
        return initExtensionsTime;
    }

    /**
     * @param initExtensionsTime overall extensions init time
     */
    public void setInitExtensionsTime(final Duration initExtensionsTime) {
        this.initExtensionsTime = initExtensionsTime;
    }

    /**
     * @return overall installers init time
     */
    public Duration getInitInstallersTime() {
        return initInstallersTime;
    }

    /**
     * @param initInstallersTime overall installers init time
     */
    public void setInitInstallersTime(final Duration initInstallersTime) {
        this.initInstallersTime = initInstallersTime;
    }

    /**
     * @return time between last dw bundle init and first run (config and environment creation)
     */
    public Duration getDwPreRunTime() {
        return dwPreRunTime;
    }

    /**
     * @param dwPreRunTime time between last dw bundle init and first run (config and environment creation)
     */
    public void setDwPreRunTime(final Duration dwPreRunTime) {
        this.dwPreRunTime = dwPreRunTime;
    }

    /**
     * @return jersey time
     */
    public Duration getJerseyTime() {
        return jerseyTime;
    }

    /**
     * @param jerseyTime jersey time
     */
    public void setJerseyTime(final Duration jerseyTime) {
        this.jerseyTime = jerseyTime;
    }

    /**
     * @return jersey startup time
     */
    public Duration getLifecycleTime() {
        return lifecycleTime;
    }

    /**
     * @param lifecycleTime jersey startup time
     */
    public void setLifecycleTime(final Duration lifecycleTime) {
        this.lifecycleTime = lifecycleTime;
    }

    /**
     * @return managed objects startup times
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
     * @return guicey events executed during initialization
     */
    public List<Class> getInitEvents() {
        return initEvents;
    }

    /**
     * @return guicey events executed during run
     */
    public List<Class> getRunEvents() {
        return runEvents;
    }

    /**
     * @return guicey events executed during web start
     */
    public List<Class> getWebEvents() {
        return webEvents;
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
