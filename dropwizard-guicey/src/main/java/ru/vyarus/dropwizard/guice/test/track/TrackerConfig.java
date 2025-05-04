package ru.vyarus.dropwizard.guice.test.track;

import java.time.temporal.ChronoUnit;

/**
 * {@link ru.vyarus.dropwizard.guice.test.track.Tracker} configuration.
 *
 * @author Vyacheslav Rusakov
 * @since 30.04.2025
 */
public class TrackerConfig {

    private boolean trace;
    private long slowMethods = 5;
    private ChronoUnit slowMethodsUnit = ChronoUnit.SECONDS;
    private boolean keepRawObjects = true;
    private int maxStringLength = 30;

    /**
     * When enabled, prints called method just after it's execution (with called arguments and returned result).
     * Not enabled by default to avoid output mess in case when many methods would be called during test.
     *
     * @return true to print each method execution
     */
    public boolean isTrace() {
        return trace;
    }

    /**
     * @param trace true to print each method execution
     */
    public void setTrace(final boolean trace) {
        this.trace = trace;
    }

    /**
     * Print warnings about methods executing longer than the specified threshold. Set to 0 to disable warnings.
     *
     * @return slow method threshold (in seconds, by default - see {@link #getSlowMethodsUnit()})
     */
    public long getSlowMethods() {
        return slowMethods;
    }

    /**
     * @param slowMethods slow method threshold (in seconds, by default - see {@link #getSlowMethodsUnit()})
     */
    public void setSlowMethods(final long slowMethods) {
        this.slowMethods = slowMethods;
    }

    /**
     * Unit for {@link #getSlowMethods()} threshold value (seconds by default).
     *
     * @return unit for threshold value
     */
    public ChronoUnit getSlowMethodsUnit() {
        return slowMethodsUnit;
    }

    /**
     * @param slowMethodsUnit unit for threshold value
     */
    public void setSlowMethodsUnit(final ChronoUnit slowMethodsUnit) {
        this.slowMethodsUnit = slowMethodsUnit;
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
     * @return true to keep raw arguments and result objects
     */
    public boolean isKeepRawObjects() {
        return keepRawObjects;
    }

    /**
     * @param keepRawObjects true to keep raw arguments and result objects
     */
    public void setKeepRawObjects(final boolean keepRawObjects) {
        this.keepRawObjects = keepRawObjects;
    }

    /**
     * Required to keep called method toString rendering readable in case of large strings used.
     * Note that for non-string objects, an object type with identity hash would be shown (not rely on toString
     * because it would be too much unpredictable).
     *
     * @return maximum length of string in method parameter or returned result
     */
    public int getMaxStringLength() {
        return maxStringLength;
    }

    /**
     * @param maxStringLength maximum length of string in method parameter or returned result
     */
    public void setMaxStringLength(final int maxStringLength) {
        this.maxStringLength = maxStringLength;
    }
}
