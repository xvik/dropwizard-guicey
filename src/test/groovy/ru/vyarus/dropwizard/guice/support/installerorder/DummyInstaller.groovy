package ru.vyarus.dropwizard.guice.support.installerorder

import ru.vyarus.dropwizard.guice.module.installer.FeatureInstaller
import ru.vyarus.dropwizard.guice.module.installer.order.Order

/**
 * Installer should run after ManagedInstaller(20) and before JerseyProviderInstaller(30)
 *
 * @author Vyacheslav Rusakov 
 * @since 17.04.2015
 */
@Order(25)
class DummyInstaller implements FeatureInstaller<DummyInstaller>{

    @Override
    boolean matches(Class<?> type) {
        return false
    }

    @Override
    void report() {
    }
}
