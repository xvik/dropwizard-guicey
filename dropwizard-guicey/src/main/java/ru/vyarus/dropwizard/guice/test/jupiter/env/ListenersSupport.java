package ru.vyarus.dropwizard.guice.test.jupiter.env;

import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import org.junit.jupiter.api.extension.ExtensionContext;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.conf.GuiceyTestTime;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.conf.TestExtensionsTracker;

import java.util.ArrayList;
import java.util.List;

/**
 * Guicey test listeners support object.
 *
 * @author Vyacheslav Rusakov
 * @since 07.02.2025
 */
public class ListenersSupport {

    private final List<TestExecutionListener> listeners = new ArrayList<>();
    private final TestExtensionsTracker tracker;

    public ListenersSupport(final TestExtensionsTracker tracker) {
        this.tracker = tracker;
    }

    public void addListener(final TestExecutionListener listener) {
        listeners.add(Preconditions.checkNotNull(listener, "Listener must not be null"));
    }

    public void broadcastStart(final ExtensionContext context) {
        if (!listeners.isEmpty()) {
            final Stopwatch timer = Stopwatch.createStarted();
            listeners.forEach(listener -> listener.started(context));
            tracker.performanceTrack(GuiceyTestTime.TestListeners, timer.elapsed());
        }
    }

    public void broadcastBeforeAll(final ExtensionContext context) {
        if (!listeners.isEmpty()) {
            final Stopwatch timer = Stopwatch.createStarted();
            listeners.forEach(listener -> listener.beforeAll(context));
            // start and before could be under same beforeAll
            tracker.performanceTrack(GuiceyTestTime.TestListeners, timer.elapsed(), true);
        }
    }

    public void broadcastBefore(final ExtensionContext context) {
        if (!listeners.isEmpty()) {
            final Stopwatch timer = Stopwatch.createStarted();
            listeners.forEach(listener -> listener.beforeEach(context));
            // start and before could be under same beforeAll
            tracker.performanceTrack(GuiceyTestTime.TestListeners, timer.elapsed(), true);
        }
    }

    public void broadcastAfter(final ExtensionContext context) {
        if (!listeners.isEmpty()) {
            final Stopwatch timer = Stopwatch.createStarted();
            listeners.forEach(listener -> listener.afterEach(context));
            tracker.performanceTrack(GuiceyTestTime.TestListeners, timer.elapsed());
        }
    }

    public void broadcastAfterAll(final ExtensionContext context) {
        if (!listeners.isEmpty()) {
            final Stopwatch timer = Stopwatch.createStarted();
            listeners.forEach(listener -> listener.afterAll(context));
            // start and before could be under same beforeAll
            tracker.performanceTrack(GuiceyTestTime.TestListeners, timer.elapsed(), true);
        }
    }

    public void broadcastStop(final ExtensionContext context) {
        if (!listeners.isEmpty()) {
            final Stopwatch timer = Stopwatch.createStarted();
            listeners.forEach(listener -> listener.stopped(context));
            // after and stop could be under the same afterEach
            tracker.performanceTrack(GuiceyTestTime.TestListeners, timer.elapsed(), true);
        }
    }
}
