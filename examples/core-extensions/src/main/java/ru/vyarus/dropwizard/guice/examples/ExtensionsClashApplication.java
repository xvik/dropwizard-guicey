package ru.vyarus.dropwizard.guice.examples;

import com.google.inject.AbstractModule;
import io.dropwizard.core.Application;
import io.dropwizard.core.Configuration;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;
import ru.vyarus.dropwizard.guice.GuiceBundle;
import ru.vyarus.dropwizard.guice.examples.rest.MyResource;
import ru.vyarus.dropwizard.guice.examples.rest.MyResourceFromBinding;
import ru.vyarus.dropwizard.guice.examples.rest.scan.MyResourceFromScan;

/**
 * @author Vyacheslav Rusakov
 * @since 30.12.2019
 */
public class ExtensionsClashApplication extends Application<Configuration> {

    @Override
    public void initialize(Bootstrap<Configuration> bootstrap) {
        bootstrap.addBundle(GuiceBundle.builder()
                // scan will find everything
                .enableAutoConfig()
                // all three directly registered
                .extensions(MyResource.class, MyResourceFromScan.class, MyResourceFromBinding.class)
                // and all three set as bindings
                .modules(new AppModule(), new AbstractModule() {
                    @Override
                    protected void configure() {
                        bind(MyResource.class);
                        bind(MyResourceFromScan.class);
                    }
                })

                // to show configured extensions
                .printDiagnosticInfo()
                .build());
    }

    @Override
    public void run(Configuration configuration, Environment environment) throws Exception {
    }
}
