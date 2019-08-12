package ru.vyarus.dropwizard.guice.module.lifecycle.event.configuration;

import io.dropwizard.ConfiguredBundle;
import io.dropwizard.setup.Bootstrap;
import ru.vyarus.dropwizard.guice.module.context.option.Options;
import ru.vyarus.dropwizard.guice.module.lifecycle.GuiceyLifecycle;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.ConfigurationPhaseEvent;

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

    public DropwizardBundlesInitializedEvent(final Options options,
                                             final Bootstrap bootstrap,
                                             final List<ConfiguredBundle> bundles,
                                             final List<ConfiguredBundle> disabled) {
        super(GuiceyLifecycle.DropwizardBundlesInitialized, options, bootstrap);
        this.bundles = bundles;
        this.disabled = disabled;
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
}
