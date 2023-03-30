package ru.vyarus.dropwizard.guice.examples;

import io.dropwizard.core.Application;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;
import ru.vyarus.dropwizard.guice.GuiceBundle;
import ru.vyarus.dropwizard.guice.examples.hbn.HbnBundle;
import ru.vyarus.dropwizard.guice.examples.hbn.HbnModule;

/**
 * Application demonstrates dropwizard hibernate bundle integration with guice.
 *
 * @author Vyacheslav Rusakov
 * @since 12.06.2016
 */
public class HbnApplication extends Application<HbnAppConfiguration> {

    @Override
    public void initialize(Bootstrap<HbnAppConfiguration> bootstrap) {
        final HbnBundle hibernate = new HbnBundle();
        // register hbn bundle before guice to make sure factory initialized before guice context start
        bootstrap.addBundle(hibernate);
        bootstrap.addBundle(GuiceBundle.builder()
                .enableAutoConfig()
                .modules(new HbnModule(hibernate))
                .build());
    }

    @Override
    public void run(HbnAppConfiguration configuration, Environment environment) throws Exception {
    }
}
