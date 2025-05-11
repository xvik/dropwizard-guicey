package ru.vyarus.dropwizard.guice.module.installer.bundle.listener;

import com.google.common.base.Throwables;
import io.dropwizard.core.Configuration;
import ru.vyarus.dropwizard.guice.module.lifecycle.GuiceyLifecycleAdapter;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.run.ApplicationRunEvent;

/**
 * {@link GuiceyStartupListener} adapter for guicey lifecycle.
 *
 * @param <C> configuration type
 * @author Vyacheslav Rusakov
 * @since 28.09.2019
 */
public class GuiceyStartupListenerAdapter<C extends Configuration> extends GuiceyLifecycleAdapter {
    private final GuiceyStartupListener<C> listener;

    /**
     * Create adapter.
     *
     * @param listener listener
     */
    public GuiceyStartupListenerAdapter(final GuiceyStartupListener<C> listener) {
        this.listener = listener;
    }

    @Override
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
