package ru.vyarus.dropwizard.guice.bundles.transitive.support

import io.dropwizard.Application
import io.dropwizard.Configuration
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle

/**
 * @author Vyacheslav Rusakov
 * @since 26.06.2016
 */
class TransitiveBundlesApp extends Application<Configuration> {

    @Override
    void initialize(Bootstrap<Configuration> bootstrap) {
        bootstrap.addBundle(GuiceBundle.builder()
                .noDefaultInstallers()
                .bundles(new Bundle1())
                .build())
    }

    @Override
    void run(Configuration configuration, Environment environment) throws Exception {
    }
}
