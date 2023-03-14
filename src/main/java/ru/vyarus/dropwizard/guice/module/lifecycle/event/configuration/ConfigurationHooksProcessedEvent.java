package ru.vyarus.dropwizard.guice.module.lifecycle.event.configuration;

import ru.vyarus.dropwizard.guice.hook.GuiceyConfigurationHook;
import ru.vyarus.dropwizard.guice.module.lifecycle.GuiceyLifecycle;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.GuiceyLifecycleEvent;
import ru.vyarus.dropwizard.guice.module.lifecycle.internal.EventsContext;

import java.util.Set;

/**
 * Appeared just in time of {@link ru.vyarus.dropwizard.guice.GuiceBundle.Builder#build()} after manual
 * builder configuration and all {@link GuiceyConfigurationHook} processing. Not called when no hooks were used.
 * <p>
 * Provides list of all used hooks.
 * <p>
 * Note: dropwizard {@link io.dropwizard.core.setup.Bootstrap} object is already existing at that moment, but bundle
 * don't have access for it yet and so it's not available in event.
 *
 * @author Vyacheslav Rusakov
 * @since 20.04.2018
 */
public class ConfigurationHooksProcessedEvent extends GuiceyLifecycleEvent {

    private final Set<GuiceyConfigurationHook> hooks;

    public ConfigurationHooksProcessedEvent(final EventsContext context, final Set<GuiceyConfigurationHook> hooks) {
        super(GuiceyLifecycle.ConfigurationHooksProcessed, context);
        this.hooks = hooks;
    }

    /**
     * @return set of all used hooks
     */
    public Set<GuiceyConfigurationHook> getHooks() {
        return hooks;
    }
}
