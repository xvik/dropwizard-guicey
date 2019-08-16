package ru.vyarus.dropwizard.guice.module.lifecycle.event.jersey;

import com.google.inject.Injector;
import io.dropwizard.Configuration;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.glassfish.jersey.internal.inject.InjectionManager;
import ru.vyarus.dropwizard.guice.module.context.option.Options;
import ru.vyarus.dropwizard.guice.module.lifecycle.GuiceyLifecycle;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.JerseyPhaseEvent;
import ru.vyarus.dropwizard.guice.module.yaml.ConfigurationTree;

/**
 * Called after complete dropwizard startup. Actually the same as jetty lifecycle started event (
 * {@link org.eclipse.jetty.util.component.AbstractLifeCycle.AbstractLifeCycleListener#lifeCycleStarted(
 *org.eclipse.jetty.util.component.LifeCycle)}), which is called after complete jetty startup.
 * May be used as assured "started" point (after all initializations). For example, to report something. This event
 * also will be casted in guicey tests ({@link ru.vyarus.dropwizard.guice.test.GuiceyAppRule}) when web part is not
 * started.
 *
 * @author Vyacheslav Rusakov
 * @since 16.08.2019
 */
public class ApplicationStartedEvent extends JerseyPhaseEvent {

    public ApplicationStartedEvent(final Options options,
                                   final Bootstrap bootstrap,
                                   final Configuration configuration,
                                   final ConfigurationTree configurationTree,
                                   final Environment environment,
                                   final Injector injector,
                                   final InjectionManager injectionManager) {
        super(GuiceyLifecycle.ApplicationStarted, options, bootstrap, configuration, configurationTree,
                environment, injector, injectionManager);
    }
}
