package ru.vyarus.dropwizard.guice.diagnostic.support

import io.dropwizard.core.Application
import io.dropwizard.core.Configuration
import io.dropwizard.core.setup.Bootstrap
import io.dropwizard.core.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.diagnostic.support.bundle.FooBundle
import ru.vyarus.dropwizard.guice.diagnostic.support.features.FooModule
import ru.vyarus.dropwizard.guice.diagnostic.support.features.FooResource
import ru.vyarus.dropwizard.guice.module.installer.feature.LifeCycleInstaller

/**
 * @author Vyacheslav Rusakov
 * @since 26.06.2016
 */
class AutoScanAppWithLookup extends Application<Configuration> {

    @Override
    void initialize(Bootstrap<Configuration> bootstrap) {
        bootstrap.addBundle(GuiceBundle.builder()
                // test bundles from abstract test lookup is enabled by default
                .enableAutoConfig(FooResource.package.name)
                .modules(new FooModule())
                .bundles(new FooBundle()) // in contrast to manual test testing with extra bundle
                .disableInstallers(LifeCycleInstaller)
                .build())
    }

    @Override
    void run(Configuration configuration, Environment environment) throws Exception {
    }
}
