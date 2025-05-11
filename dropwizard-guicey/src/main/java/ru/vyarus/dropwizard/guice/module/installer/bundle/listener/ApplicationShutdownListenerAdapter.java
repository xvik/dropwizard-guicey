package ru.vyarus.dropwizard.guice.module.installer.bundle.listener;

import com.google.common.base.Throwables;
import ru.vyarus.dropwizard.guice.module.lifecycle.GuiceyLifecycleAdapter;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.jersey.ApplicationStoppedEvent;

/**
 * {@link ApplicationShutdownListener} adapter for guicey lifecycle.
 *
 * @author Vyacheslav Rusakov
 * @since 24.02.2025
 */
public class ApplicationShutdownListenerAdapter extends GuiceyLifecycleAdapter {

    private final ApplicationShutdownListener listener;

    /**
     * Create adapter.
     *
     * @param listener listener
     */
    public ApplicationShutdownListenerAdapter(final ApplicationShutdownListener listener) {
        this.listener = listener;
    }

    @Override
    protected void applicationStopped(final ApplicationStoppedEvent event) {
        try {
            listener.stopped(event.getInjector());
        } catch (Exception e) {
            Throwables.throwIfUnchecked(e);
            throw new IllegalStateException("Failed to process startup listener", e);
        }
    }

    @Override
    public String toString() {
        return "ShutdownListener(" + listener.getClass().getSimpleName() + ")";
    }
}
