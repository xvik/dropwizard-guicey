package ru.vyarus.dropwizard.guice.examples.petstore;

import org.glassfish.jersey.CommonProperties;
import ru.vyarus.dropwizard.guice.examples.ExampleConfig;
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBundle;
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyEnvironment;

/**
 * @author Vyacheslav Rusakov
 * @since 07.05.2025
 */
public class PetStoreBundle implements GuiceyBundle {

    @Override
    public void run(GuiceyEnvironment environment) throws Exception {
        // because of required conflicting dependency jersey-media-json-jackson
        // https://github.com/dropwizard/dropwizard/issues/1341#issuecomment-251503011
        environment.environment().jersey()
                .property(CommonProperties.FEATURE_AUTO_DISCOVERY_DISABLE, Boolean.TRUE);

        // register client api
        environment.modules(new PetStoreApiModule());

        // optional fake server start
        if (environment.<ExampleConfig>configuration().isStartFakeStore()) {
            environment.register(FakePetStoreServer.class);
        }
    }
}
