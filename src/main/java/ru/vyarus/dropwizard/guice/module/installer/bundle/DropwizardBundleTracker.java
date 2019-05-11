package ru.vyarus.dropwizard.guice.module.installer.bundle;

import io.dropwizard.Configuration;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

import java.util.function.Supplier;

/**
 * @author Vyacheslav Rusakov
 * @since 07.05.2019
 */
public class DropwizardBundleTracker<T extends Configuration> implements ConfiguredBundle<T> {

    private ConfiguredBundle<? super T> bundle;
    private Supplier<Bootstrap<T>> bootstrap;

    public DropwizardBundleTracker(ConfiguredBundle<? super T> bundle, Supplier<Bootstrap<T>> bootstrap) {
        this.bundle = bundle;
        this.bootstrap = bootstrap;
    }

    @Override
    public void initialize(Bootstrap bootstrap) {
        // use proxied bootstrap to intercept transitive bundle registrations
        bundle.initialize(this.bootstrap.get());
    }

    @Override
    public void run(T configuration, Environment environment) throws Exception {
        bundle.run(configuration, environment);
    }
}
