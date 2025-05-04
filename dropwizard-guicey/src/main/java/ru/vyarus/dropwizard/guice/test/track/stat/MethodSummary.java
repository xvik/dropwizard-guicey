package ru.vyarus.dropwizard.guice.test.track.stat;

import com.codahale.metrics.Snapshot;
import com.codahale.metrics.Timer;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import ru.vyarus.dropwizard.guice.test.track.MethodTrack;
import ru.vyarus.dropwizard.guice.test.util.PrintUtils;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Tracked method summary (for all calls of exact method).
 *
 * @author Vyacheslav Rusakov
 * @since 12.02.2025
 */
@SuppressFBWarnings("EQ_COMPARETO_USE_OBJECT_EQUALS")
public class MethodSummary implements Comparable<MethodSummary> {

    private final Class<?> service;
    private final Method method;
    private final Set<String> instances = new HashSet<>();

    private int tracks;
    private int errors;
    private final Snapshot snapshot;


    public MethodSummary(final Class<?> service, final Method method, final Timer timer) {
        this.service = service;
        this.method = method;
        this.snapshot = timer.getSnapshot();
    }

    /**
     * Used during summary object aggregation.
     *
     * @param track track to append to summary
     */
    public void add(final MethodTrack track) {
        tracks++;
        if (!track.isSuccess()) {
            errors++;
        }
        instances.add(track.getInstanceHash());
    }

    /**
     * @return method bean class
     */
    public Class<?> getService() {
        return service;
    }

    /**
     * @return method itself
     */
    public Method getMethod() {
        return method;
    }

    /**
     * @return registered tracks count
     */
    public int getTracks() {
        return tracks;
    }

    /**
     * @return failed calls count
     */
    public int getErrors() {
        return errors;
    }

    /**
     * @return metrics timer snapshot (for reporting)
     */
    public Snapshot getMetrics() {
        return snapshot;
    }

    /**
     * @return minimal method call duration (string representation)
     */
    public String getMin() {
        return PrintUtils.formatMetric(snapshot.getMin());
    }

    /**
     * @return maximum method call duration (string representation)
     */
    public String getMax() {
        return PrintUtils.formatMetric(snapshot.getMax());
    }

    /**
     * @return median method call duration (string representation)
     */
    public String getMedian() {
        return PrintUtils.formatMetric(snapshot.getMedian());
    }

    /**
     * In test, the first execution would be slow (jvm warm up). This percentile should show more or less
     * correct (hot) method execution time, in case of many executions (useful for "raw performance" checks).
     *
     * @return duration of 75% method calls (string representation)
     */
    public String get75thPercentile() {
        return PrintUtils.formatMetric(snapshot.get75thPercentile());
    }

    /**
     * @return duration of 95% method calls (string representation)
     */
    public String get95thPercentile() {
        return PrintUtils.formatMetric(snapshot.get95thPercentile());
    }

    /**
     * @return number of different bean instances used for method calls
     */
    public int getInstancesCount() {
        return instances.size();
    }

    /**
     * Method used for console reporting. Return type is not included because the method could be identified by
     * arguments only.
     *
     * @return string representation for method (with argument types, but without return type)
     */
    public String toStringMethod() {
        return method.getName() + "("
                + Arrays.stream(method.getParameterTypes()).map(Class::getSimpleName).collect(Collectors.joining(", "))
                + ")";
    }

    @Override
    public String toString() {
        return toStringMethod() + " called " + tracks + " times" + (errors > 0 ? " (" + errors + ")" : "")
                + (instances.size() > 1 ? " (on " + instances.size() + " instances)" : "");
    }

    @Override
    public int compareTo(final MethodSummary o) {
        return Double.compare(snapshot.getMedian(), o.snapshot.getMedian());
    }
}
