package ru.vyarus.dropwizard.guice.module.installer.bundle.listener;

import com.google.common.base.Throwables;
import ru.vyarus.dropwizard.guice.module.lifecycle.GuiceyLifecycleAdapter;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.run.ApplicationRunEvent;

/**
 * {@link GuiceyStartupListener} adapter for guicey lifecycle.
 *
 * @author Vyacheslav Rusakov
 * @since 28.09.2019
 */
public class GuiceyStartupListenerAdapter extends GuiceyLifecycleAdapter {
    private final GuiceyStartupListener listener;

    public GuiceyStartupListenerAdapter(final GuiceyStartupListener listener) {
        this.listener = listener;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void applicationRun(final ApplicationRunEvent event) {
        try {
            listener.configure(event.getConfiguration(), event.getEnvironment(), event.getInjector());
        } catch (Exception e) {
            Throwables.throwIfUnchecked(e);
            throw new IllegalStateException("Failed to process guicey startup listener", e);
        }
    }

    @Override
    public String toString() {
        return "GuiceyStartupListener(" + listener.getClass().getSimpleName() + ")";
    }
}
