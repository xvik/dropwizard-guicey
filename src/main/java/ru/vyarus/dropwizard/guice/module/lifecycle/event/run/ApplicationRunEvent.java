package ru.vyarus.dropwizard.guice.module.lifecycle.event.run;

import com.google.inject.Injector;
import io.dropwizard.Configuration;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.eclipse.jetty.util.component.LifeCycle;
import org.glassfish.jersey.server.monitoring.ApplicationEventListener;
import ru.vyarus.dropwizard.guice.module.context.option.Options;
import ru.vyarus.dropwizard.guice.module.lifecycle.GuiceyLifecycle;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.InjectorPhaseEvent;
import ru.vyarus.dropwizard.guice.module.yaml.ConfigurationTree;

/**
 * Called after
 * {@link ru.vyarus.dropwizard.guice.GuiceBundle#run(io.dropwizard.Configuration, io.dropwizard.setup.Environment)}
 * when guicey context is started, extensions installed (but not hk extensions, because neither jersey nor jetty
 * isn't start yet).
 * <p>
 * At this point injection to registered commands is performed (this may be important if custom command
 * run application instead of "server"). Injector itseld is completely initialized - all singletons processed.
 * <p>
 * This point is before
 * {@link io.dropwizard.Application#run(io.dropwizard.Configuration, io.dropwizard.setup.Environment)}. Ideal point
 * for jersey and jetty listeners installation  (use shortcut methods in event for registration).
 *
 * @author Vyacheslav Rusakov
 * @see ru.vyarus.dropwizard.guice.module.lifecycle.debug.DebugGuiceyLifecycle for listeners usage example
 * @since 19.04.2018
 */
public class ApplicationRunEvent extends InjectorPhaseEvent {

    public ApplicationRunEvent(final Options options,
                               final Bootstrap bootstrap,
                               final Configuration configuration,
                               final ConfigurationTree configurationTree,
                               final Environment environment,
                               final Injector injector) {
        super(GuiceyLifecycle.ApplicationRun, options, bootstrap,
                configuration, configurationTree, environment, injector);
    }

    /**
     * @param listener jetty listener
     * @see org.eclipse.jetty.util.component.AbstractLifeCycle.AbstractLifeCycleListener
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
