package ru.vyarus.dropwizard.guice.module.lifecycle.event.configuration;

import io.dropwizard.setup.Bootstrap;
import ru.vyarus.dropwizard.guice.module.context.option.Options;
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBundle;
import ru.vyarus.dropwizard.guice.module.lifecycle.GuiceyLifecycle;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.InitPhaseEvent;

import java.util.List;

/**
 * Called when all bundles are resolved (after dw recognition and lookup). Called even if no bundles
 * registered. Provides list of all top-level enabled bundles and list of disabled bundles.
 * <p>
 * Bundles may be post-processed here by modifying it's state with some interface (maybe based on other bundles
 * analysis).
 *
 * @author Vyacheslav Rusakov
 * @see BundlesFromLookupResolvedEvent called earlier
 * @since 19.04.2018
 */
public class BundlesResolvedEvent extends InitPhaseEvent {

    private final List<GuiceyBundle> bundles;
    private final List<GuiceyBundle> disabled;

    public BundlesResolvedEvent(final Options options,
                                final Bootstrap bootstrap,
                                final List<GuiceyBundle> bundles,
                                final List<GuiceyBundle> disabled) {
        super(GuiceyLifecycle.BundlesResolved, options, bootstrap);
        this.bundles = bundles;
        this.disabled = disabled;
    }

    /**
     * @return all top-level enabled bundles
     */
    public List<GuiceyBundle> getBundles() {
        return bundles;
    }

    /**
     * Note: bundles are not yet processed so more bundles could be disabled later.
     *
     * @return list of disabled bundles or empty list
     */
    public List<GuiceyBundle> getDisabled() {
        return disabled;
    }
}
