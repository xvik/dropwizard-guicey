package ru.vyarus.dropwizard.guice.examples;

import io.dropwizard.core.Application;
import io.dropwizard.core.Configuration;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;
import ru.vyarus.dropwizard.guice.GuiceBundle;
import ru.vyarus.dropwizard.guice.examples.rest.SampleResource;
import ru.vyarus.dropwizard.guice.module.installer.feature.jersey.ResourceInstaller;

/**
 * Manual mode sample application.
 *
 * @author Vyacheslav Rusakov
 * @since 27.01.2016
 */
public class InstallersResetApplication extends Application<Configuration> {

    @Override
    public void initialize(Bootstrap<Configuration> bootstrap) {
        bootstrap.addBundle(GuiceBundle.builder()
                // disable all installers except resources
                .noDefaultInstallers()
                .installers(ResourceInstaller.class)

                .extensions(SampleResource.class)
                // see all registered installers
                .printAvailableInstallers()
                .build());
    }

    @Override
    public void run(Configuration configuration, Environment environment) throws Exception {
    }
}
