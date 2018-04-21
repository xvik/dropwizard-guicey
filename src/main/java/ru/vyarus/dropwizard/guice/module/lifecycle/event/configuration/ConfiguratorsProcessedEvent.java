package ru.vyarus.dropwizard.guice.module.lifecycle.event.configuration;

import ru.vyarus.dropwizard.guice.module.context.option.OptionsInfo;
import ru.vyarus.dropwizard.guice.module.lifecycle.GuiceyLifecycle;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.GuiceyLifecycleEvent;
import ru.vyarus.dropwizard.guice.module.support.conf.GuiceyConfigurator;

import java.util.Set;

/**
 * Appeared just in time of {@link ru.vyarus.dropwizard.guice.GuiceBundle.Builder#build()} after manual
 * builder configuration and all {@link GuiceyConfigurator} processing. Not called when no configurators were used.
 * <p>
 * Provides list of all used configurators.
 * <p>
 * Note: dropwizard {@link io.dropwizard.setup.Bootstrap} object is already existing at that moment, but bundle
 * don't have access for it yet and so it's not available in event.
 *
 * @author Vyacheslav Rusakov
 * @since 20.04.2018
 */
public class ConfiguratorsProcessedEvent extends GuiceyLifecycleEvent {

    private final Set<GuiceyConfigurator> configurators;

    public ConfiguratorsProcessedEvent(final OptionsInfo options, final Set<GuiceyConfigurator> configurators) {
        super(GuiceyLifecycle.ConfiguratorsProcessed, options);
        this.configurators = configurators;
    }

    /**
     * @return set of all used configurators
     */
    public Set<GuiceyConfigurator> getConfigurators() {
        return configurators;
    }
}
