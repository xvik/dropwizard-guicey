package ru.vyarus.dropwizard.guice.module.lifecycle.event.run;

import io.dropwizard.Configuration;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import ru.vyarus.dropwizard.guice.module.context.option.Options;
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBundle;
import ru.vyarus.dropwizard.guice.module.lifecycle.GuiceyLifecycle;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.RunPhaseEvent;
import ru.vyarus.dropwizard.guice.module.yaml.ConfigurationTree;

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

    public BundlesStartedEvent(final Options options,
                               final Bootstrap bootstrap,
                               final Configuration configuration,
                               final ConfigurationTree configurationTree,
                               final Environment environment,
                               final List<GuiceyBundle> bundles) {
        super(GuiceyLifecycle.BundlesStarted, options, bootstrap, configuration, configurationTree, environment);
        this.bundles = bundles;
    }

    /**
     * @return list of all started bundles
     */
    public List<GuiceyBundle> getBundles() {
        return bundles;
    }
}
