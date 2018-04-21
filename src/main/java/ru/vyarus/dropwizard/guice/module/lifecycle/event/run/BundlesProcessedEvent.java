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
 * Called after bundles processing. Note that bundles could register other bundles and so resulted
 * list of installed bundles could be bigger (than in resolution event). Provides a list of all used bundles.
 * Called even if no bundles were used at all (to indicate major lifecycle point).
 * <p>
 * May be used for consultation only as bundles are not used anymore (already processed).
 *
 * @author Vyacheslav Rusakov
 * @since 19.04.2018
 */
public class BundlesProcessedEvent extends RunPhaseEvent {

    private final List<GuiceyBundle> bundles;

    public BundlesProcessedEvent(final OptionsInfo options,
                                 final Bootstrap bootstrap,
                                 final Configuration configuration,
                                 final Environment environment,
                                 final List<GuiceyBundle> bundles) {
        super(GuiceyLifecycle.BundlesProcessed, options, bootstrap, configuration, environment);
        this.bundles = bundles;
    }

    /**
     * @return list of all used bundles (actually processed bundles, including transitives) or empty list if
     * bundles were not used at all
     */
    public List<GuiceyBundle> getBundles() {
        return bundles;
    }
}
