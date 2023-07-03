package ru.vyarus.guice.dropwizard.examples;

import io.dropwizard.core.Application;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;
import ru.vyarus.dropwizard.guice.GuiceBundle;
import ru.vyarus.guice.dropwizard.examples.module.RootModule;

/**
 * @author Vyacheslav Rusakov
 * @since 03.07.2023
 */
public class SampleApplication extends Application<SampleConfiguration> {

    public static void main(String[] args) throws Exception {
        new SampleApplication().run(args);
    }


    @Override
    public void initialize(Bootstrap<SampleConfiguration> bootstrap) {
        bootstrap.addBundle(GuiceBundle.builder()
                .enableAutoConfig()
                .modules(new RootModule())
                .build());
    }

    @Override
    public void run(SampleConfiguration configuration, Environment environment) throws Exception {
    }
}
