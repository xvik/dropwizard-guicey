package ru.vyarus.dropwizard.guice.test.jupiter.setup.client.rest;

import io.dropwizard.core.Application;
import io.dropwizard.core.Configuration;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import ru.vyarus.dropwizard.guice.GuiceBundle;

/**
 * @author Vyacheslav Rusakov
 * @since 17.10.2025
 */
public class RestApp extends Application<Configuration> {

    @Override
    public void initialize(Bootstrap<Configuration> bootstrap) {
        bootstrap.addBundle(GuiceBundle.builder()
                .extensions(Resource.class)
                .build());
    }

    @Override
    public void run(Configuration configuration, Environment environment) throws Exception {
    }

    @Path("/resource")
    public static class Resource {

        @GET
        public String get() {
            return "ok";
        }
    }
}
