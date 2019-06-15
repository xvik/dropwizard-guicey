package ru.vyarus.dropwizard.guice.module.lifecycle.event.configuration;

import io.dropwizard.setup.Bootstrap;
import ru.vyarus.dropwizard.guice.module.context.option.Options;
import ru.vyarus.dropwizard.guice.module.lifecycle.GuiceyLifecycle;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.ConfigurationPhaseEvent;

/**
 * Called after guicey initialization (includes bundles lookup and initialization,
 * installers and extensions resolution). Pure marker event, indicating guicey work finished under dropwizard
 * configuration phase.
 * <p>
 * Note: dropwizard bundles, registered after {@link ru.vyarus.dropwizard.guice.GuiceBundle} will be initialized
 * after this point.
 *
 * @author Vyacheslav Rusakov
 * @since 19.04.2018
 */
public class InitializedEvent extends ConfigurationPhaseEvent {

    public InitializedEvent(final Options options, final Bootstrap bootstrap) {
        super(GuiceyLifecycle.Initialized, options, bootstrap);
    }
}
