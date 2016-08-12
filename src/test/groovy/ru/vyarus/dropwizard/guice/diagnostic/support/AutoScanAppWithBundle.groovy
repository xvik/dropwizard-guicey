package ru.vyarus.dropwizard.guice.diagnostic.support

import io.dropwizard.Application
import io.dropwizard.Configuration
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.diagnostic.support.bundle.FooBundle
import ru.vyarus.dropwizard.guice.diagnostic.support.features.FooModule
import ru.vyarus.dropwizard.guice.diagnostic.support.features.FooResource
import ru.vyarus.dropwizard.guice.module.installer.feature.LifeCycleInstaller

/**
 * @author Vyacheslav Rusakov
 * @since 26.06.2016
 */
class AutoScanAppWithBundle extends Application<Configuration> {

    @Override
    void initialize(Bootstrap<Configuration> bootstrap) {
        bootstrap.addBundle(GuiceBundle.builder()
        // no default test bundles from abstract test
                .disableBundleLookup()
                .enableAutoConfig(FooResource.package.name)
                .bundles(new FooBundle())
                .modules(new FooModule())
                .disableInstallers(LifeCycleInstaller)
                .build())
    }

    @Override
    void run(Configuration configuration, Environment environment) throws Exception {
    }
}
