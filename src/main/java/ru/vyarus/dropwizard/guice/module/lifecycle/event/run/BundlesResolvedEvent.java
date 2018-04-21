package ru.vyarus.dropwizard.guice.module.lifecycle.event.run;

import io.dropwizard.Configuration;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import ru.vyarus.dropwizard.guice.module.context.option.OptionsInfo;
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBundle;
import ru.vyarus.dropwizard.guice.module.lifecycle.GuiceyLifecycle;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.RunPhaseEvent;

import java.util.List;

/**
 * Called when all bundles are resolved (after dw recognition and lookup). Will not be called if no bundles
 * registered at all. Provides list of all top-level enabled bundles.
 * <p>
 * Bundles may be post-processed here by modifying it's state with some interface (maybe based on other bundles
 * analysis).
 *
 * @author Vyacheslav Rusakov
 * @see BundlesFromDwResolvedEvent called earlier
 * @see BundlesFromLookupResolvedEvent called earlier
 * @since 19.04.2018
 */
public class BundlesResolvedEvent extends RunPhaseEvent {

    private final List<GuiceyBundle> bundles;

    public BundlesResolvedEvent(final OptionsInfo options,
                                final Bootstrap bootstrap,
                                final Configuration configuration,
                                final Environment environment,
                                final List<GuiceyBundle> bundles) {
        super(GuiceyLifecycle.BundlesResolved, options, bootstrap, configuration, environment);
        this.bundles = bundles;
    }

    /**
     * @return all top-level enabled bundles
     */
    public List<GuiceyBundle> getBundles() {
        return bundles;
    }
}
