package ru.vyarus.dropwizard.guice.examples;

import io.dropwizard.core.Application;
import io.dropwizard.core.Configuration;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;
import ru.vyarus.dropwizard.guice.GuiceBundle;
import ru.vyarus.dropwizard.guice.examples.view.ComplexIndexView;
import ru.vyarus.guicey.gsp.ServerPagesBundle;

/**
 * @author Vyacheslav Rusakov
 * @since 22.10.2020
 */
public class GspSpaApplication extends Application<Configuration> {

    public static void main(String[] args) throws Exception {
        new GspSpaApplication().run(args);
    }

    @Override
    public void initialize(Bootstrap<Configuration> bootstrap) {

        bootstrap.addBundle(GuiceBundle.builder()
                // view rest for example 3
                .extensions(ComplexIndexView.class)
                .bundles(
                        // global views support
                        ServerPagesBundle.builder().build(),
                        // application registration
                        ServerPagesBundle.app("app", "/app/", "/app/")
                                // index page is now freemarker template
                                .indexPage("index.ftl")
                                .spaRouting()
                                .build(),

                        // same application registration on different url
                        ServerPagesBundle.app("app2", "/app/", "/app2/")
                                // index page is now freemarker template
                                .indexPage("index.ftl")
                                .spaRouting()
                                .build(),

                        // same application but with index page served as view
                        ServerPagesBundle.app("app3", "/app/", "/app3/")
                                // index page is now freemarker template
                                .mapViews("/views/app3/") // <-- map rest views for application
                                .indexPage("/index/")  // <-- rest view path, not actual page
                                .spaRouting()
                                .build()
                )
                .build());
    }


    @Override
    public void run(Configuration configuration, Environment environment) throws Exception {

    }
}
