package ru.vyarus.dropwizard.guice.module.lifecycle.event.jersey;

import ru.vyarus.dropwizard.guice.module.lifecycle.GuiceyLifecycle;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.JerseyPhaseEvent;
import ru.vyarus.dropwizard.guice.module.lifecycle.internal.EventsContext;

/**
 * Called after complete application configuration ({@link io.dropwizard.core.Application#run(
 * io.dropwizard.core.Configuration, io.dropwizard.core.setup.Environment)} called), but before lifecycle  startup
 * (before managed objects run). Actually the same as jetty lifecycle started event
 * ({@link org.eclipse.jetty.util.component.LifeCycle.Listener#lifeCycleStarting(
 * org.eclipse.jetty.util.component.LifeCycle)}.
 * <p>
 * May be used as for additional services startup (after all initializations), executed before "started" point,
 * often used for reporting. As an example, sub rest use this event to run jersey context after initialization.
 * This event also will be fired in guicey tests ({@link ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp}
 * which does not start the web part).
 *
 * @author Vyacheslav Rusakov
 * @since 16.10.2025
 */
public class ApplicationStartingEvent extends JerseyPhaseEvent {

    /**
     * Create event.
     *
     * @param context even context
     */
    public ApplicationStartingEvent(final EventsContext context) {
        super(GuiceyLifecycle.ApplicationStarting, context);
    }
}
