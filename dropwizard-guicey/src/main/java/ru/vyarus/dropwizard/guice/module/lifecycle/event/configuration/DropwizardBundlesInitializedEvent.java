package ru.vyarus.dropwizard.guice.module.lifecycle.event.configuration;

import io.dropwizard.core.ConfiguredBundle;
import ru.vyarus.dropwizard.guice.module.lifecycle.GuiceyLifecycle;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.ConfigurationPhaseEvent;
import ru.vyarus.dropwizard.guice.module.lifecycle.internal.EventsContext;

import java.util.List;

/**
 * Called after dropwizard bundles initialization (for dropwizard bundles registered through guicey api).
 * Not called if no bundles were registered.
 * <p>
 * Note that bundles will be processed further under dropwizard run phase so at this point bundles state could be
 * modified to affect execution.
 *
 * @author Vyacheslav Rusakov
 * @since 12.08.2019
 */
public class DropwizardBundlesInitializedEvent extends ConfigurationPhaseEvent {

    private final List<ConfiguredBundle> bundles;
    private final List<ConfiguredBundle> disabled;
    private final List<ConfiguredBundle> ignored;

    /**
     * Create event.
     *
     * @param context  event context
     * @param bundles  actual bundles
     * @param disabled disabled bundles
     * @param ignored  ignored bundles (duplicates)
     */
    public DropwizardBundlesInitializedEvent(final EventsContext context,
                                             final List<ConfiguredBundle> bundles,
                                             final List<ConfiguredBundle> disabled,
                                             final List<ConfiguredBundle> ignored) {
        super(GuiceyLifecycle.DropwizardBundlesInitialized, context);
        this.bundles = bundles;
        this.disabled = disabled;
        this.ignored = ignored;
    }

    /**
     * Note that transitive bundles would be also included (if bundles tracking not disabled
     * {@link ru.vyarus.dropwizard.guice.GuiceyOptions#TrackDropwizardBundles}).
     *
     * @return initialized dropwizard bundles (excluding disabled)
     */
    public List<ConfiguredBundle> getBundles() {
        return bundles;
    }

    /**
     * @return disabled dropwizard bundles or empty list
     */
    public List<ConfiguredBundle> getDisabled() {
        return disabled;
    }

    /**
     * @return ignored dropwizard bundles (duplicates) or empty list
     */
    public List<ConfiguredBundle> getIgnored() {
        return ignored;
    }
}
