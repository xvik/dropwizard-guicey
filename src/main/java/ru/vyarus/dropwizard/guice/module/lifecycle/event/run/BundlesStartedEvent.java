package ru.vyarus.dropwizard.guice.module.lifecycle.event.run;

import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBundle;
import ru.vyarus.dropwizard.guice.module.lifecycle.GuiceyLifecycle;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.RunPhaseEvent;
import ru.vyarus.dropwizard.guice.module.lifecycle.internal.EventsContext;

import java.util.List;

/**
 * Called after bundles start (run method call). Not called if no bundles were used at all.
 * <p>
 * May be used for consultation only as bundles are not used anymore (already processed).
 *
 * @author Vyacheslav Rusakov
 * @since 13.06.2019
 */
public class BundlesStartedEvent extends RunPhaseEvent {

    private final List<GuiceyBundle> bundles;

    public BundlesStartedEvent(final EventsContext context,
                               final List<GuiceyBundle> bundles) {
        super(GuiceyLifecycle.BundlesStarted, context);
        this.bundles = bundles;
    }

    /**
     * @return list of all started bundles
     */
    public List<GuiceyBundle> getBundles() {
        return bundles;
    }
}
