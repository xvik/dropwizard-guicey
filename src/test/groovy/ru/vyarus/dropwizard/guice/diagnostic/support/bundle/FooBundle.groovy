package ru.vyarus.dropwizard.guice.diagnostic.support.bundle

import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBootstrap
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBundle
import ru.vyarus.dropwizard.guice.module.installer.feature.ManagedInstaller

/**
 * @author Vyacheslav Rusakov
 * @since 26.06.2016
 */
class FooBundle implements GuiceyBundle {

    @Override
    void initialize(GuiceyBootstrap bootstrap) {
        bootstrap
                .bundles(new FooBundleRelativeBundle())
                .installers(FooBundleInstaller)
                .extensions(FooBundleResource)
                .modules(new FooBundleModule())
                .disableInstallers(ManagedInstaller)
    }

    @Override
    boolean equals(Object obj) {
        // only one FooBundle registration allowed
        return obj.getClass().equals(FooBundle.class)
    }
}
