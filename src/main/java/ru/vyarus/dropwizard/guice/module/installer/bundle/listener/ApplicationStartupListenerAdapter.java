package ru.vyarus.dropwizard.guice.module.installer.bundle.listener;

import com.google.common.base.Throwables;
import ru.vyarus.dropwizard.guice.module.lifecycle.GuiceyLifecycleAdapter;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.jersey.ApplicationStartedEvent;

/**
 * {@link ApplicationStartupListener} adapter for guicey lifecycle.
 *
 * @author Vyacheslav Rusakov
 * @since 28.09.2019
 */
public class ApplicationStartupListenerAdapter extends GuiceyLifecycleAdapter {
    private final ApplicationStartupListener listener;

    public ApplicationStartupListenerAdapter(final ApplicationStartupListener listener) {
        this.listener = listener;
    }

    @Override
    protected void applicationStarted(final ApplicationStartedEvent event) {
        try {
            listener.started(event.getInjector());
        } catch (Exception e) {
            Throwables.throwIfUnchecked(e);
            throw new IllegalStateException("Failed to process startup listener", e);
        }
    }

    @Override
    public String toString() {
        return "StartupListener(" + listener.getClass().getSimpleName() + ")";
    }
}
