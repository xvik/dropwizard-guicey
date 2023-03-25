package ru.vyarus.dropwizard.guice.diagnostic.support.bundle

import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBootstrap
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBundle
import ru.vyarus.dropwizard.guice.module.installer.feature.ManagedInstaller

/**
 * @author Vyacheslav Rusakov
 * @since 02.08.2016
 */
class Foo2Bundle implements GuiceyBundle {

    @Override
    void initialize(GuiceyBootstrap bootstrap) {
        bootstrap
                .bundles(new FooBundleRelative2Bundle())
                .installers(FooBundleInstaller)
                .extensions(FooBundleResource)
                .modules(new FooBundleModule())
                .disableInstallers(ManagedInstaller)
    }
}
