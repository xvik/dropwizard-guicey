package ru.vyarus.dropwizard.guice.module.lifecycle.event.run;

import io.dropwizard.Configuration;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import ru.vyarus.dropwizard.guice.module.context.option.Options;
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBundle;
import ru.vyarus.dropwizard.guice.module.lifecycle.GuiceyLifecycle;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.RunPhaseEvent;

import java.util.List;

/**
 * Called if configuration from dw bundles enabled and at least one bundle recognized (not called at all (!) if
 * nothing initialized). Provides list of recognized bundles.
 * <p>
 * Note: some of these bundles could be actually disabled and not used further.
 *
 * @author Vyacheslav Rusakov
 * @since 21.04.2018
 */
public class BundlesFromDwResolvedEvent extends RunPhaseEvent {

    private final List<GuiceyBundle> bundles;

    public BundlesFromDwResolvedEvent(final Options options,
                                      final Bootstrap bootstrap,
                                      final Configuration configuration,
                                      final Environment environment,
                                      final List<GuiceyBundle> bundles) {
        super(GuiceyLifecycle.BundlesFromDwResolved, options, bootstrap, configuration, environment);
        this.bundles = bundles;
    }

    /**
     * @return recognized dropwizard bundles
     */
    public List<GuiceyBundle> getBundles() {
        return bundles;
    }
}
