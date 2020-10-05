package ru.vyarus.dropwizard.guice.module.lifecycle.event.jersey;

import ru.vyarus.dropwizard.guice.module.lifecycle.GuiceyLifecycle;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.JerseyPhaseEvent;
import ru.vyarus.dropwizard.guice.module.lifecycle.internal.EventsContext;

/**
 * Called after application shutdown. Triggered by jetty lifecycle stopping event (
 * {@link org.eclipse.jetty.util.component.AbstractLifeCycle.AbstractLifeCycleListener#lifeCycleStopping(
 * org.eclipse.jetty.util.component.LifeCycle)}).
 * <p>
 * Supposed to be used to cleanup some resources after complete shutdown (very specific cases).
 *
 * @author Vyacheslav Rusakov
 * @since 05.10.2020
 */
public class ApplicationStoppedEvent extends JerseyPhaseEvent {

    public ApplicationStoppedEvent(final EventsContext context) {
        super(GuiceyLifecycle.ApplicationStopped, context);
    }
}
