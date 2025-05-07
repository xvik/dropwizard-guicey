package ru.vyarus.dropwizard.guice.examples;

import io.dropwizard.core.Application;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;
import ru.vyarus.dropwizard.guice.GuiceBundle;
import ru.vyarus.dropwizard.guice.examples.petstore.PetStoreApiModule;
import ru.vyarus.dropwizard.guice.examples.petstore.PetStoreBundle;

/**
 * @author Vyacheslav Rusakov
 * @since 07.05.2025
 */
public class ExampleApp extends Application<ExampleConfig> {

    @Override
    public void initialize(Bootstrap<ExampleConfig> bootstrap) {
        bootstrap.addBundle(GuiceBundle.builder()
                .bundles(new PetStoreBundle())
                .build());
    }

    @Override
    public void run(ExampleConfig config, Environment environment) throws Exception {

    }
}
