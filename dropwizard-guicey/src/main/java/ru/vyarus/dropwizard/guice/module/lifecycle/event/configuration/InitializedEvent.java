package ru.vyarus.dropwizard.guice.module.lifecycle.event.configuration;

import ru.vyarus.dropwizard.guice.module.lifecycle.GuiceyLifecycle;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.ConfigurationPhaseEvent;
import ru.vyarus.dropwizard.guice.module.lifecycle.internal.EventsContext;

/**
 * Called after guicey initialization (includes bundles lookup and initialization,
 * installers and extensions resolution). Pure marker event, indicating guicey work finished under dropwizard
 * configuration phase.
 * <p>
 * Note: dropwizard bundles, registered after {@link ru.vyarus.dropwizard.guice.GuiceBundle} will be initialized
 * after this point.
 *
 * @author Vyacheslav Rusakov
 * @since 19.04.2018
 */
public class InitializedEvent extends ConfigurationPhaseEvent {

    /**
     * Create event.
     *
     * @param context event context
     */
    public InitializedEvent(final EventsContext context) {
        super(GuiceyLifecycle.Initialized, context);
    }
}
