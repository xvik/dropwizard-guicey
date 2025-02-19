package ru.vyarus.dropwizard.guice.support;

import io.dropwizard.core.Application;
import io.dropwizard.core.Configuration;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;
import ru.vyarus.dropwizard.guice.GuiceBundle;


/**
 * Default test application (without configurations).
 *
 * @author Vyacheslav Rusakov
 * @since 18.02.2025
 */
public class DefaultTestApp extends Application<Configuration> {

    @Override
    public void initialize(Bootstrap<Configuration> bootstrap) {
        bootstrap.addBundle(configure());
    }

    protected GuiceBundle configure() {
        return GuiceBundle.builder().build();
    }

    @Override
    public void run(Configuration configuration, Environment environment) throws Exception {
    }
}
