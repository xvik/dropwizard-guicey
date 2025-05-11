package ru.vyarus.dropwizard.guice.module.lifecycle.event.configuration;

import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBundle;
import ru.vyarus.dropwizard.guice.module.lifecycle.GuiceyLifecycle;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.ConfigurationPhaseEvent;
import ru.vyarus.dropwizard.guice.module.lifecycle.internal.EventsContext;

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
public class BundlesResolvedEvent extends ConfigurationPhaseEvent {

    private final List<GuiceyBundle> bundles;
    private final List<GuiceyBundle> disabled;
    private final List<GuiceyBundle> ignored;

    /**
     * Create event.
     *
     * @param context  event context
     * @param bundles  actual bundles
     * @param disabled disabled bundles
     * @param ignored  ignored bundles (duplicates)
     */
    public BundlesResolvedEvent(final EventsContext context,
                                final List<GuiceyBundle> bundles,
                                final List<GuiceyBundle> disabled,
                                final List<GuiceyBundle> ignored) {
        super(GuiceyLifecycle.BundlesResolved, context);
        this.bundles = bundles;
        this.disabled = disabled;
        this.ignored = ignored;
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

    /**
     * Note: bundles are not yet processed so more bundles could be ignored later.
     *
     * @return list of ignored bundles (duplicates) or empty list
     */
    public List<GuiceyBundle> getIgnored() {
        return ignored;
    }
}
