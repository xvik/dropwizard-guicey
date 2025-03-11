package ru.vyarus.dropwizard.guice.debug.report.start;

import com.google.common.base.Stopwatch;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.dropwizard.lifecycle.JettyManaged;
import io.dropwizard.lifecycle.setup.LifecycleEnvironment;
import org.eclipse.jetty.util.component.LifeCycle;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EventListener;
import java.util.List;

/**
 * Managed objects tracker for startup time report. Replaces managed objects list inside
 * {@link io.dropwizard.lifecycle.setup.LifecycleEnvironment} to wrap existing and future managed (and lifecycle)
 * objects (to be able to track start and stop executions).
 *
 * @author Vyacheslav Rusakov
 * @since 10.03.2025
 */
@SuppressFBWarnings({"CT_CONSTRUCTOR_THROW", "EQ_DOESNT_OVERRIDE_EQUALS", "SE_BAD_FIELD"})
public class ManagedTracker extends ArrayList<LifeCycle> {

    private final StartupTimeInfo start;
    private final ShutdownTimeInfo stop;

    public ManagedTracker(final StartupTimeInfo start,
                          final ShutdownTimeInfo stop,
                          final LifecycleEnvironment lifecycle) {
        this.start = start;
        this.stop = stop;
        injectTracker(lifecycle);
    }

    @Override
    public boolean add(final LifeCycle lifeCycle) {
        // wraps managed or lifecycle object to track its execution
        return super.add(new LifeCycleTracker(lifeCycle));
    }

    @Override
    public void add(final int index, final LifeCycle element) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(final Collection<? extends LifeCycle> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(final int index, final Collection<? extends LifeCycle> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public LifeCycle set(final int index, final LifeCycle element) {
        throw new UnsupportedOperationException();
    }

    private void injectTracker(final LifecycleEnvironment lifecycle) {
        try {
            final Field managedObjects = LifecycleEnvironment.class.getDeclaredField("managedObjects");
            managedObjects.setAccessible(true);
            final List<LifeCycle> existing = (List<LifeCycle>) managedObjects.get(lifecycle);
            if (!existing.isEmpty()) {
                // pack with tracker (to measure start/stop execution)
                existing.forEach(this::add);
            }
            managedObjects.set(lifecycle, this);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to inject managed objects tracker", e);
        }
    }

    /**
     * Wrapper for dropwizard lifecycle objects (managed). Used to record start/stop execution times.
     */
    private class LifeCycleTracker implements LifeCycle {

        private final LifeCycle object;
        private final boolean managed;
        private final Class<?> type;

        LifeCycleTracker(final LifeCycle object) {
            this.object = object;
            managed = object instanceof JettyManaged;
            type = managed ? ((JettyManaged) object).getManaged().getClass() : object.getClass();
        }

        @Override
        public void start() throws Exception {
            final Stopwatch timer = Stopwatch.createStarted();
            object.start();
            start.getManagedTimes().put(type, timer.stop().elapsed());
            start.getManagedTypes().put(type, managed ? "managed" : "lifecycle");
        }

        @Override
        public void stop() throws Exception {
            final Stopwatch timer = Stopwatch.createStarted();
            object.stop();
            stop.getManagedTimes().put(type, timer.stop().elapsed());
            stop.getManagedTypes().put(type, managed ? "managed" : "lifecycle");
        }

        @Override
        public boolean isRunning() {
            return object.isRunning();
        }

        @Override
        public boolean isStarted() {
            return object.isStarted();
        }

        @Override
        public boolean isStarting() {
            return object.isStarting();
        }

        @Override
        public boolean isStopping() {
            return object.isStopping();
        }

        @Override
        public boolean isStopped() {
            return object.isStopped();
        }

        @Override
        public boolean isFailed() {
            return object.isFailed();
        }

        @Override
        public boolean addEventListener(final EventListener listener) {
            return object.addEventListener(listener);
        }

        @Override
        public boolean removeEventListener(final EventListener listener) {
            return object.removeEventListener(listener);
        }
    }
}
