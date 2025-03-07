package ru.vyarus.dropwizard.guice.test.jupiter.ext.conf.track;

import ru.vyarus.dropwizard.guice.test.util.PrintUtils;

import java.time.Duration;

/**
 * Depending on test, application could be instantiated before all or before each test methods.
 * When executing methods of the same test, indicating increased time (e.g., each beforeEach).
 */
@SuppressWarnings("VisibilityModifier")
class PerformanceTrack {
    final GuiceyTestTime name;
    final GuiceyTestTime phase;
    // for simplicity, tracking increase between logs (that's what we actually need)
    Duration loggedDuration;
    Duration duration;

    PerformanceTrack(final GuiceyTestTime name, final GuiceyTestTime phase) {
        this.name = name;
        this.phase = phase;
    }

    void registerDuration(final Duration duration) {
        this.duration = this.duration == null ? duration : this.duration.plus(duration);
    }

    boolean isDurationChanged() {
        return loggedDuration == null || loggedDuration.compareTo(duration) < 0;
    }

    void markLogged() {
        loggedDuration = duration;
    }

    boolean isRoot() {
        return phase == name;
    }

    Duration getOverall() {
        return duration;
    }

    Duration getIncrease() {
        return loggedDuration == null ? duration : duration.minus(loggedDuration);
    }

    @Override
    public String toString() {
        String title = name.getDisplayName();
        if (isRoot()) {
            title = "[" + title + "]";
        }
        return String.format("%-35s: %s", title, PrintUtils.renderTime(getOverall(),
                loggedDuration == null ? null : getIncrease()));
    }
}
