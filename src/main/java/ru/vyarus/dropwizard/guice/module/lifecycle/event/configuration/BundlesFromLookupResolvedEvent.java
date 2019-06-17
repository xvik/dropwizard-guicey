package ru.vyarus.dropwizard.guice.module.lifecycle.event.configuration;

import io.dropwizard.setup.Bootstrap;
import ru.vyarus.dropwizard.guice.module.context.option.Options;
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBundle;
import ru.vyarus.dropwizard.guice.module.lifecycle.GuiceyLifecycle;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.ConfigurationPhaseEvent;

import java.util.List;

/**
 * Called if at least one bundle recognized using bundles lookup (not called at all (!) if
 * nothing found). Provides list of found bundles.
 * <p>
 * Note: some of these bundles could be actually disabled and not used further.
 *
 * @author Vyacheslav Rusakov
 * @since 21.04.2018
 */
public class BundlesFromLookupResolvedEvent extends ConfigurationPhaseEvent {

    private final List<GuiceyBundle> bundles;

    public BundlesFromLookupResolvedEvent(final Options options,
                                          final Bootstrap bootstrap,
                                          final List<GuiceyBundle> bundles) {
        super(GuiceyLifecycle.BundlesFromLookupResolved, options, bootstrap);
        this.bundles = bundles;
    }

    /**
     * @return list of bundles found on lookup
     */
    public List<GuiceyBundle> getBundles() {
        return bundles;
    }
}
