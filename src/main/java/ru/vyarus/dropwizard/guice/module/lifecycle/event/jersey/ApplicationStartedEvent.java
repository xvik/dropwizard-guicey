package ru.vyarus.dropwizard.guice.module.lifecycle.event.jersey;

import ru.vyarus.dropwizard.guice.module.lifecycle.GuiceyLifecycle;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.JerseyPhaseEvent;
import ru.vyarus.dropwizard.guice.module.lifecycle.internal.EventsContext;

/**
 * Called after complete dropwizard startup. Actually the same as jetty lifecycle started event (
 * {@link org.eclipse.jetty.util.component.AbstractLifeCycle.AbstractLifeCycleListener#lifeCycleStarted(
 *org.eclipse.jetty.util.component.LifeCycle)}), which is called after complete jetty startup.
 * <p>
 * May be used as assured "started" point (after all initializations). For example, to report something. This event
 * also will be casted in guicey tests ({@link ru.vyarus.dropwizard.guice.test.GuiceyAppRule}) when web part is not
 * started.
 *
 * @author Vyacheslav Rusakov
 * @since 16.08.2019
 */
public class ApplicationStartedEvent extends JerseyPhaseEvent {

    public ApplicationStartedEvent(final EventsContext context) {
        super(GuiceyLifecycle.ApplicationStarted, context);
    }

    /**
     * As event fired for both real server startup and guicey lightweight tests, this property allows
     * to differentiate situations.
     *
     * @return true if jetty was started and false in case of guicey lightweight tests
     * @see ru.vyarus.dropwizard.guice.test.spock.UseGuiceyApp
     * @see ru.vyarus.dropwizard.guice.test.GuiceyAppRule
     */
    public boolean isJettyStarted() {
        return getEnvironment().getApplicationContext().getServer() != null
                && getEnvironment().getApplicationContext().getServer().isStarted();
    }
}
