package ru.vyarus.dropwizard.guice.module.lifecycle.event.jersey;


import ru.vyarus.dropwizard.guice.module.lifecycle.GuiceyLifecycle;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.JerseyPhaseEvent;
import ru.vyarus.dropwizard.guice.module.lifecycle.internal.EventsContext;

/**
 * Called on application shutdown start. Triggered by jetty lifecycle stopping event (
 * {@link org.eclipse.jetty.util.component.AbstractLifeCycle.AbstractLifeCycleListener#lifeCycleStopping(
 * org.eclipse.jetty.util.component.LifeCycle)}).
 * <p>
 * May be used to perform some shutdown logic.
 *
 * @author Vyacheslav Rusakov
 * @since 25.10.2019
 */
public class ApplicationShotdownEvent extends JerseyPhaseEvent {

    public ApplicationShotdownEvent(final EventsContext context) {
        super(GuiceyLifecycle.ApplicationShutdown, context);
    }

    /**
     * As event fired for both real server startup and guicey lightweight tests, this property allows
     * differentiating situations.
     *
     * @return true if jetty was started and false in case of guicey lightweight tests
     * @see ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp
     */
    public boolean isJettyStarted() {
        return getEnvironment().getApplicationContext().getServer() != null
                && getEnvironment().getApplicationContext().getServer().isStopping();
    }
}
