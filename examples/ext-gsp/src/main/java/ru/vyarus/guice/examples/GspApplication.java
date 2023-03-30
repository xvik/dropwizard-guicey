package ru.vyarus.guice.examples;

import io.dropwizard.core.Application;
import io.dropwizard.core.Configuration;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;
import ru.vyarus.dropwizard.guice.GuiceBundle;
import ru.vyarus.guicey.gsp.ServerPagesBundle;

/**
 * @author Vyacheslav Rusakov
 * @since 23.10.2019
 */
public class GspApplication extends Application<Configuration> {

    public static void main(String[] args) throws Exception {
        new GspApplication().run(args);
    }

    @Override
    public void initialize(Bootstrap<Configuration> bootstrap) {
        bootstrap.addBundle(GuiceBundle.builder()
                .enableAutoConfig()
                .bundles(
                        // global views support
                        ServerPagesBundle.builder().build(),
                        // application registration
                        ServerPagesBundle.app("app", "/app/", "/")
                                // rest path as index page
                                .indexPage("person/")
                                .build())
                .build());
    }

    @Override
    public void run(Configuration configuration, Environment environment) throws Exception {

    }
}
