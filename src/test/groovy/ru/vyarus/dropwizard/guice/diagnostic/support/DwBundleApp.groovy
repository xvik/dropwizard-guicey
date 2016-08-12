package ru.vyarus.dropwizard.guice.diagnostic.support

import io.dropwizard.Application
import io.dropwizard.Bundle
import io.dropwizard.Configuration
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBootstrap
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBundle

/**
 * @author Vyacheslav Rusakov
 * @since 11.07.2016
 */
class DwBundleApp extends Application<Configuration> {

    @Override
    void initialize(Bootstrap<Configuration> bootstrap) {
        bootstrap.addBundle(new DwBundle())
        bootstrap.addBundle(GuiceBundle.builder()
                .noDefaultInstallers()
                .configureFromDropwizardBundles()
                .build())
    }

    @Override
    void run(Configuration configuration, Environment environment) throws Exception {
    }

    public static class DwBundle implements GuiceyBundle, Bundle {

        @Override
        void initialize(Bootstrap<?> bootstrap) {
        }

        @Override
        void run(Environment environment) {
        }

        @Override
        void initialize(GuiceyBootstrap bootstrap) {
        }
    }
}
