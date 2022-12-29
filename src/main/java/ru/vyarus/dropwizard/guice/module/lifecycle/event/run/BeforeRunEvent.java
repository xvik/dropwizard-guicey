package ru.vyarus.dropwizard.guice.module.lifecycle.event.run;

import ru.vyarus.dropwizard.guice.module.lifecycle.GuiceyLifecycle;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.RunPhaseEvent;
import ru.vyarus.dropwizard.guice.module.lifecycle.internal.EventsContext;

/**
 * Meta event. Called just before guice bundle processing in run phase. Convenient point before main guicey logic.
 *
 * @author Vyacheslav Rusakov
 * @since 13.06.2018
 */
public class BeforeRunEvent extends RunPhaseEvent {

    public BeforeRunEvent(final EventsContext context) {
        super(GuiceyLifecycle.BeforeRun, context);
    }
}
