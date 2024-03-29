package ru.vyarus.dropwizard.guice.examples;

import io.dropwizard.core.Application;
import io.dropwizard.core.Configuration;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;
import ru.vyarus.dropwizard.guice.GuiceBundle;
import ru.vyarus.guicey.eventbus.EventBusBundle;

/**
 * @author Vyacheslav Rusakov
 * @since 07.03.2017
 */
public class EventBusApp extends Application<Configuration> {

    public static void main(String[] args) throws Exception {
        new EventBusApp().run(args);
    }

    @Override
    public void initialize(Bootstrap<Configuration> bootstrap) {
        bootstrap.addBundle(GuiceBundle.builder()
                .enableAutoConfig()
                .bundles(new EventBusBundle())
                .build());
    }

    @Override
    public void run(Configuration configuration, Environment environment) throws Exception {
    }
}
