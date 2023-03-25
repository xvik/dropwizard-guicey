package ru.vyarus.dropwizard.guice.module.lifecycle.event;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jersey.internal.inject.InjectionManager;
import ru.vyarus.dropwizard.guice.module.lifecycle.GuiceyLifecycle;
import ru.vyarus.dropwizard.guice.module.lifecycle.internal.EventsContext;

/**
 * Base class for events, started after jersey context initialization start. Appears after jetty start, during
 * jersey initialization.
 *
 * @author Vyacheslav Rusakov
 * @since 19.04.2018
 */
public abstract class JerseyPhaseEvent extends InjectorPhaseEvent {

    private final InjectionManager injectionManager;

    public JerseyPhaseEvent(final GuiceyLifecycle type,
                            final EventsContext context) {
        super(type, context);
        this.injectionManager = context.getInjectionManager();
    }

    /**
     * Note: all guicey events are happen before jersey application initialization finish and so manager can't be
     * used for extensions access, but it could be stored somewhere and used later (with help of jersey lifecycle
     * listener).
     * <p>
     * Note: HK2 {@link ServiceLocator} could be obtained as bean from manager as
     * {@code getInjectionManager().getInstance(ServiceLocator.class)}.
     *
     * @return root service locator
     */
    public InjectionManager getInjectionManager() {
        return injectionManager;
    }
}
