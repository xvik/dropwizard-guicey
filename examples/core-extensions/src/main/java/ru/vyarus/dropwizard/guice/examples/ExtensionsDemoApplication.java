package ru.vyarus.dropwizard.guice.examples;

import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import ru.vyarus.dropwizard.guice.GuiceBundle;
import ru.vyarus.dropwizard.guice.examples.rest.MyResource;

/**
 * Autoconfig mode sample application.
 *
 * @author Vyacheslav Rusakov
 * @since 27.01.2016
 */
public class ExtensionsDemoApplication extends Application<Configuration> {

    public static void main(String[] args) throws Exception {
        new ExtensionsDemoApplication().run("server");
    }

    @Override
    public void initialize(Bootstrap<Configuration> bootstrap) {
        bootstrap.addBundle(GuiceBundle.builder()
                // direct resource registration
                .extensions(MyResource.class)
                // scan must find another resource
                .enableAutoConfig("ru.vyarus.dropwizard.guice.examples.rest.scan")
                // third resource bound in module
                .modules(new AppModule())

                // to show configured extensions
                .printDiagnosticInfo()
                .build());
    }

    @Override
    public void run(Configuration configuration, Environment environment) throws Exception {
    }
}
