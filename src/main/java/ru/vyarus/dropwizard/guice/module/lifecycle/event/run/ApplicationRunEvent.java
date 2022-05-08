package ru.vyarus.dropwizard.guice.module.lifecycle.event.run;

import org.eclipse.jetty.util.component.LifeCycle;
import org.glassfish.jersey.server.monitoring.ApplicationEventListener;
import ru.vyarus.dropwizard.guice.module.lifecycle.GuiceyLifecycle;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.InjectorPhaseEvent;
import ru.vyarus.dropwizard.guice.module.lifecycle.internal.EventsContext;

/**
 * Called after
 * {@link ru.vyarus.dropwizard.guice.GuiceBundle#run(io.dropwizard.Configuration, io.dropwizard.setup.Environment)}
 * when guicey context is started, extensions installed (but not hk extensions, because neither jersey nor jetty
 * isn't start yet).
 * <p>
 * At this point injection to registered commands is performed (this may be important if custom command
 * run application instead of "server"). Injector itself is completely initialized - all singletons processed.
 * <p>
 * This point is before
 * {@link io.dropwizard.Application#run(io.dropwizard.Configuration, io.dropwizard.setup.Environment)}. Ideal point
 * for jersey and jetty listeners installation  (use shortcut methods in event for registration).
 *
 * @author Vyacheslav Rusakov
 * @see ru.vyarus.dropwizard.guice.debug.LifecycleDiagnostic for listeners usage example
 * @since 19.04.2018
 */
public class ApplicationRunEvent extends InjectorPhaseEvent {

    public ApplicationRunEvent(final EventsContext context) {
        super(GuiceyLifecycle.ApplicationRun, context);
    }

    /**
     * @param listener jetty listener
     */
    public void registerJettyListener(final LifeCycle.Listener listener) {
        getEnvironment().lifecycle().addLifeCycleListener(listener);
    }

    /**
     * @param listener jersey listener
     * @see org.glassfish.jersey.server.monitoring.ApplicationEvent.Type for available events
     */
    public void registerJerseyListener(final ApplicationEventListener listener) {
        getEnvironment().jersey().register(listener);
    }
}
