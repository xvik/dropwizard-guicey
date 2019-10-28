package ru.vyarus.dropwizard.guice.module.lifecycle.event.jersey;

import org.glassfish.jersey.internal.inject.InjectionManager;
import ru.vyarus.dropwizard.guice.module.lifecycle.GuiceyLifecycle;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.JerseyPhaseEvent;
import ru.vyarus.dropwizard.guice.module.lifecycle.internal.EventsContext;

/**
 * Jersey context starting. At this point jersey and jetty is only initializing. Guicey jersey configuration
 * is not yer performed. Since that point jersey {@link InjectionManager} is accessible.
 *
 * @author Vyacheslav Rusakov
 * @since 19.04.2018
 */
public class JerseyConfigurationEvent extends JerseyPhaseEvent {

    public JerseyConfigurationEvent(final EventsContext context) {
        super(GuiceyLifecycle.JerseyConfiguration, context);
    }
}
