package ru.vyarus.dropwizard.guice.module.lifecycle.event.configuration;

import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBundle;
import ru.vyarus.dropwizard.guice.module.lifecycle.GuiceyLifecycle;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.ConfigurationPhaseEvent;
import ru.vyarus.dropwizard.guice.module.lifecycle.internal.EventsContext;

import java.util.List;

/**
 * Called after bundles initialization. Note that bundles could register other bundles and so resulted
 * list of installed bundles could be bigger (than in resolution event). Provides a list of all used and list of
 * disabled bundles. Not called if no bundles were used at all (nothing was processed - no event).
 * <p>
 * Note that bundles will be processed further under dropwizard run phase so at this point bundles state could be
 * modified to affect execution.
 *
 * @author Vyacheslav Rusakov
 * @since 13.06.2019
 */
public class BundlesInitializedEvent extends ConfigurationPhaseEvent {

    private final List<GuiceyBundle> bundles;
    private final List<GuiceyBundle> disabled;
    private final List<GuiceyBundle> ignored;

    public BundlesInitializedEvent(final EventsContext context,
                                   final List<GuiceyBundle> bundles,
                                   final List<GuiceyBundle> disabled,
                                   final List<GuiceyBundle> ignored) {
        super(GuiceyLifecycle.BundlesInitialized, context);
        this.bundles = bundles;
        this.disabled = disabled;
        this.ignored = ignored;
    }

    /**
     * @return list of all used bundles (actually processed bundles, including transitives) or empty list if
     * bundles were not used at all
     */
    public List<GuiceyBundle> getBundles() {
        return bundles;
    }

    /**
     * @return list of all disabled bundles or empty list
     */
    public List<GuiceyBundle> getDisabled() {
        return disabled;
    }

    /**
     * @return list of ignored bundles (duplicates) or empty list
     */
    public List<GuiceyBundle> getIgnored() {
        return ignored;
    }
}
