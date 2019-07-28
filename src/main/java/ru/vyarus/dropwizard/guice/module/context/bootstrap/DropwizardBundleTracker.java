package ru.vyarus.dropwizard.guice.module.context.bootstrap;

import io.dropwizard.Configuration;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import ru.vyarus.dropwizard.guice.module.context.ConfigurationContext;
import ru.vyarus.dropwizard.guice.module.context.info.ItemId;

/**
 * Bundle decorator. Used to track transitive dropwizard bundles registration.
 * <p>
 * Bundles tracking is controlled with {@link ru.vyarus.dropwizard.guice.GuiceyOptions#TrackDropwizardBundles}
 * option.
 *
 * @author Vyacheslav Rusakov
 * @since 07.05.2019
 * @param <T> configuration type
 */
public class DropwizardBundleTracker<T extends Configuration> implements ConfiguredBundle<T> {

    private ConfiguredBundle<? super T> bundle;
    private ConfigurationContext context;

    public DropwizardBundleTracker(ConfiguredBundle<? super T> bundle, ConfigurationContext context) {
        this.bundle = bundle;
        this.context = context;
    }

    @Override
    public void initialize(final Bootstrap bootstrap) {
        final ItemId currentScope = context.replaceContextScope(ItemId.from(bundle));
        // initialize with proxy bootstrap object to intercept transitive bundles registration
        bundle.initialize(context.getBootstrapProxy());
        context.replaceContextScope(currentScope);
    }

    @Override
    public void run(final T configuration, final Environment environment) throws Exception {
        bundle.run(configuration, environment);
    }
}
