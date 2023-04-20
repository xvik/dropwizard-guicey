package ru.vyarus.dropwizard.guice.examples;

import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import ru.vyarus.dropwizard.guice.GuiceBundle;
import ru.vyarus.dropwizard.guice.examples.installer.MarkersInstaller;
import ru.vyarus.dropwizard.guice.examples.service.SampleMarker;

/**
 * Sample application for custom installer in manual config mode.
 *
 * @author Vyacheslav Rusakov
 * @since 29.01.2016
 */
public class CustomInstallerApplication extends Application<Configuration> {

    @Override
    public void initialize(Bootstrap<Configuration> bootstrap) {
        bootstrap.addBundle(GuiceBundle.builder()
                .installers(MarkersInstaller.class)
                .extensions(SampleMarker.class)
                // to show that marker was installed by custom installer
                .printDiagnosticInfo()
                .build());
    }

    @Override
    public void run(Configuration configuration, Environment environment) throws Exception {
    }
}
