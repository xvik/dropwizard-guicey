package ru.vyarus.dropwizard.guice.module.lifecycle.event.configuration;

import ru.vyarus.dropwizard.guice.module.lifecycle.GuiceyLifecycle;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.ConfigurationPhaseEvent;
import ru.vyarus.dropwizard.guice.module.lifecycle.internal.EventsContext;

/**
 * Special meta event, called before all {@link ru.vyarus.dropwizard.guice.GuiceBundle} configuration phase logic.
 * {@link io.dropwizard.core.setup.Bootstrap} object is available, but dropwizard bundles (registered through
 * guicey) are not yet registered (note that {@link ru.vyarus.dropwizard.guice.GuiceBundle} is not yet added to
 * bootstrap also because dropwizard calls bundle initialization before registering bundle (and so all dropwizard
 * bundles, registered by guicey, will run before {@link ru.vyarus.dropwizard.guice.GuiceBundle} run).
 *
 * @author Vyacheslav Rusakov
 * @since 28.02.2025
 */
public class BeforeInitEvent extends ConfigurationPhaseEvent {

    public BeforeInitEvent(final EventsContext context) {
        super(GuiceyLifecycle.BeforeInit, context);
    }
}
