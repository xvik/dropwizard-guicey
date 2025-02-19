package ru.vyarus.dropwizard.guice.test.jupiter.env;

import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import org.junit.jupiter.api.extension.ExtensionContext;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.conf.track.GuiceyTestTime;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.conf.track.TestExtensionsTracker;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Guicey test listeners support object.
 *
 * @author Vyacheslav Rusakov
 * @since 07.02.2025
 */
public class ListenersSupport {

    private final Set<TestExecutionListener> listeners = new LinkedHashSet<>();
    private final TestExtensionsTracker tracker;

    public ListenersSupport(final TestExtensionsTracker tracker) {
        this.tracker = tracker;
    }

    public void addListener(final TestExecutionListener listener) {
        listeners.add(Preconditions.checkNotNull(listener, "Listener must not be null"));
    }

    public void broadcastStart(final ExtensionContext context) {
        broadcast(listener -> listener.started(context), false);
    }

    public void broadcastBeforeAll(final ExtensionContext context) {
        broadcast(listener -> listener.beforeAll(context), true);
    }

    public void broadcastBefore(final ExtensionContext context) {
        broadcast(listener -> listener.beforeEach(context), true);
    }

    public void broadcastAfter(final ExtensionContext context) {
        broadcast(listener -> listener.afterEach(context), true);
    }

    public void broadcastAfterAll(final ExtensionContext context) {
        broadcast(listener -> listener.afterAll(context), true);
    }

    public void broadcastStop(final ExtensionContext context) {
        broadcast(listener -> listener.stopped(context), false);
    }

    private void broadcast(final Consumer<TestExecutionListener> action, final boolean append) {
        if (!listeners.isEmpty()) {
            final Stopwatch timer = Stopwatch.createStarted();
            listeners.forEach(action);
            tracker.performanceTrack(GuiceyTestTime.TestListeners, timer.elapsed(), append);
        }
    }
}
