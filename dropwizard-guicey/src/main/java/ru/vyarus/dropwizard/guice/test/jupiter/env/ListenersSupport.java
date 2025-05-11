package ru.vyarus.dropwizard.guice.test.jupiter.env;

import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import com.google.common.base.Throwables;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.function.ThrowingConsumer;
import ru.vyarus.dropwizard.guice.test.jupiter.env.listen.EventContext;
import ru.vyarus.dropwizard.guice.test.jupiter.env.listen.TestExecutionListener;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.conf.track.GuiceyTestTime;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.conf.track.TestExtensionsTracker;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Guicey test listeners support object.
 *
 * @author Vyacheslav Rusakov
 * @since 07.02.2025
 */
public class ListenersSupport {

    private final Set<TestExecutionListener> listeners = new LinkedHashSet<>();
    private final TestExtensionsTracker tracker;

    /**
     * Create support.
     *
     * @param tracker tracker
     */
    public ListenersSupport(final TestExtensionsTracker tracker) {
        this.tracker = tracker;
    }

    /**
     * Register listener.
     *
     * @param listener listener
     */
    public void addListener(final TestExecutionListener listener) {
        listeners.add(Preconditions.checkNotNull(listener, "Listener must not be null"));
    }

    /**
     * Application starting.
     *
     * @param context junit context
     */
    public void broadcastStarting(final ExtensionContext context) {
        broadcast(listener -> listener.starting(new EventContext(context, tracker.debug)));
    }

    /**
     * Application started.
     *
     * @param context junit context
     */
    public void broadcastStart(final ExtensionContext context) {
        broadcast(listener -> listener.started(new EventContext(context, tracker.debug)));
    }

    /**
     * Before all test methods.
     *
     * @param context junit context
     */
    public void broadcastBeforeAll(final ExtensionContext context) {
        broadcast(listener -> listener.beforeAll(new EventContext(context, tracker.debug)));
    }

    /**
     * Before each test method.
     *
     * @param context junit context
     */
    public void broadcastBefore(final ExtensionContext context) {
        broadcast(listener -> listener.beforeEach(new EventContext(context, tracker.debug)));
    }

    /**
     * After each test method.
     *
     * @param context junit context
     */
    public void broadcastAfter(final ExtensionContext context) {
        broadcast(listener -> listener.afterEach(new EventContext(context, tracker.debug)));
    }

    /**
     * After all test methods.
     *
     * @param context junit context
     */
    public void broadcastAfterAll(final ExtensionContext context) {
        broadcast(listener -> listener.afterAll(new EventContext(context, tracker.debug)));
    }

    /**
     * Application stopping.
     *
     * @param context junit context
     */
    public void broadcastStopping(final ExtensionContext context) {
        broadcast(listener -> listener.stopping(new EventContext(context, tracker.debug)));
    }

    /**
     * Application stopped.
     *
     * @param context junit context
     */
    public void broadcastStop(final ExtensionContext context) {
        broadcast(listener -> listener.stopped(new EventContext(context, tracker.debug)));
    }

    private void broadcast(final ThrowingConsumer<TestExecutionListener> action) {
        if (!listeners.isEmpty()) {
            final Stopwatch timer = Stopwatch.createStarted();
            listeners.forEach(l -> {
                try {
                    action.accept(l);
                } catch (Throwable ex) {
                    Throwables.throwIfUnchecked(ex);
                    throw new IllegalStateException("Failed to execute listener", ex);
                }
            });
            tracker.performanceTrack(GuiceyTestTime.TestListeners, timer.elapsed());
        }
    }
}
