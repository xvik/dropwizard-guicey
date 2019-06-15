package ru.vyarus.dropwizard.guice.module.lifecycle.event;

import io.dropwizard.setup.Bootstrap;
import ru.vyarus.dropwizard.guice.module.context.option.Options;
import ru.vyarus.dropwizard.guice.module.lifecycle.GuiceyLifecycle;

/**
 * Base class for dropwizard configuration phase events.
 *
 * @author Vyacheslav Rusakov
 * @since 13.06.2019
 */
public abstract class ConfigurationPhaseEvent extends GuiceyLifecycleEvent {

    private final Bootstrap bootstrap;

    public ConfigurationPhaseEvent(final GuiceyLifecycle type,
                                   final Options options,
                                   final Bootstrap bootstrap) {
        super(type, options);
        this.bootstrap = bootstrap;
    }

    /**
     * @return bootstrap object
     */
    public Bootstrap getBootstrap() {
        return bootstrap;
    }
}
