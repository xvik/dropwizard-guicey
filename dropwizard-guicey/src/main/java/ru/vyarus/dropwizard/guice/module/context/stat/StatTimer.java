package ru.vyarus.dropwizard.guice.module.context.stat;

import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;

/**
 * Abstraction above {@link com.google.common.base.Stopwatch} to support inlined starts
 * (when something tries to start already started timer). This is required when an initialization sequence could
 * change and some blocks become part of already measured scope (in this case, such a measure should be simply ignored,
 * as timer already counts it).
 *
 * @author Vyacheslav Rusakov
 * @since 25.02.2025
 */
public class StatTimer {
    private final Stopwatch stopwatch;
    private int started;

    /**
     * Create timer.
     *
     * @param stopwatch watch
     */
    public StatTimer(final Stopwatch stopwatch) {
        this.stopwatch = stopwatch;
    }

    /**
     * Start timer. Timer could be started multiple times (inlined scopes), but it must be stopped accordingly.
     *
     * @return timer instance
     */
    public StatTimer start() {
        synchronized (stopwatch) {
            if (started == 0) {
                stopwatch.start();
            }
            started++;
        }
        return this;
    }

    /**
     * Stop timer. Could be called multiple times if inlined scopes.
     *
     * @throws java.lang.IllegalStateException if the timer already stopped
     */
    public void stop() {
        Preconditions.checkState(started >= 1, "Timer already stopped");
        synchronized (stopwatch) {
            if (started == 1) {
                stopwatch.stop();
            }
            started--;
        }
    }

    /**
     * @return true if the timer is running
     */
    public boolean isRunning() {
        return stopwatch.isRunning();
    }

    /**
     * @return underlying stopwatch
     */
    public Stopwatch getStopwatch() {
        return stopwatch;
    }
}
