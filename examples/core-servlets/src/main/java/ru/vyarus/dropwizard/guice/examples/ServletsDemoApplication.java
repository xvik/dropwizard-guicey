package ru.vyarus.dropwizard.guice.examples;

import com.google.inject.servlet.ServletModule;
import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import ru.vyarus.dropwizard.guice.GuiceBundle;
import ru.vyarus.dropwizard.guice.examples.web.*;
import ru.vyarus.dropwizard.guice.examples.web.guice.GuiceFilter;
import ru.vyarus.dropwizard.guice.examples.web.guice.GuiceServlet;

/**
 * @author Vyacheslav Rusakov
 * @since 31.12.2019
 */
public class ServletsDemoApplication extends Application<Configuration> {

    @Override
    public void initialize(Bootstrap<Configuration> bootstrap) {
        bootstrap.addBundle(GuiceBundle.builder()
                // classpath scan or direct bindings (not in servlet module) could be used for registration instead
                .extensions(
                        SampleServlet.class,
                        SampleFilter.class,
                        // just to show registration in admin context
                        AdminServlet.class,
                        SampleRequestListener.class)

                // registration with guice ServletModule
                .modules(new ServletModule() {
                    @Override
                    protected void configureServlets() {
                        // note that if filter will be mapped as /gsample/* it will not apply to servlet call
                        serve("/gsample").with(GuiceServlet.class);
                        filter("/gsample").through(GuiceFilter.class);
                    }
                })

                // show web related registrations
                .printWebMappings()
                .build());
    }

    @Override
    public void run(Configuration configuration, Environment environment) throws Exception {

    }
}
