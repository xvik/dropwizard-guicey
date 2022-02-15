package ru.vyarus.dropwizard.guice.order

import com.google.inject.Inject
import ru.vyarus.dropwizard.guice.AbstractTest
import ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo
import ru.vyarus.dropwizard.guice.module.installer.feature.ManagedInstaller
import ru.vyarus.dropwizard.guice.module.installer.feature.jersey.JerseyFeatureInstaller
import ru.vyarus.dropwizard.guice.module.installer.feature.jersey.provider.JerseyProviderInstaller
import ru.vyarus.dropwizard.guice.module.installer.order.Order
import ru.vyarus.dropwizard.guice.support.installerorder.DummyInstaller
import ru.vyarus.dropwizard.guice.support.installerorder.OrderedInstallersApplication
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp

/**
 * @author Vyacheslav Rusakov 
 * @since 17.04.2015
 */
@TestGuiceyApp(OrderedInstallersApplication)
class InstallersOrderTest extends AbstractTest {

    @Inject
    GuiceyConfigurationInfo info

    def "Check default installers order"() {

        def pos = 0;
        info.installersOrdered.each {
            int instPos = it.getAnnotation(Order).value()
            assert instPos >= pos
            pos = instPos
        }
        expect:
        pos == 110
    }

    def "Check custom installer position correct"() {

        expect:
        info.installersOrdered[1] == ManagedInstaller
        info.installersOrdered[2] == DummyInstaller
        info.installersOrdered[3] == JerseyProviderInstaller || info.installersOrdered[3] == JerseyFeatureInstaller

    }
}