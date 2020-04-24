package ru.vyarus.dropwizard.guice.module.lifecycle.event.configuration;

import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBundle;
import ru.vyarus.dropwizard.guice.module.lifecycle.GuiceyLifecycle;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.ConfigurationPhaseEvent;
import ru.vyarus.dropwizard.guice.module.lifecycle.internal.EventsContext;

import java.util.List;

/**
 * Called if at least one bundle recognized using bundles lookup. Not called at if
 * nothing found. Provides list of found bundles.
 * <p>
 * Note: some of these bundles could be actually disabled and not used further.
 *
 * @author Vyacheslav Rusakov
 * @since 21.04.2018
 */
public class BundlesFromLookupResolvedEvent extends ConfigurationPhaseEvent {

    private final List<GuiceyBundle> bundles;

    public BundlesFromLookupResolvedEvent(final EventsContext context,
                                          final List<GuiceyBundle> bundles) {
        super(GuiceyLifecycle.BundlesFromLookupResolved, context);
        this.bundles = bundles;
    }

    /**
     * @return list of bundles found on lookup
     */
    public List<GuiceyBundle> getBundles() {
        return bundles;
    }
}
