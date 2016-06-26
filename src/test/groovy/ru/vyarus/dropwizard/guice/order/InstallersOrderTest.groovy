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
import ru.vyarus.dropwizard.guice.test.spock.UseGuiceyApp

/**
 * @author Vyacheslav Rusakov 
 * @since 17.04.2015
 */
@UseGuiceyApp(OrderedInstallersApplication)
class InstallersOrderTest extends AbstractTest {

    @Inject
    GuiceyConfigurationInfo info

    def "Check default installers order"() {

        def pos = 0;
        info.installers.each {
            int instPos = it.getAnnotation(Order).value()
            assert instPos >= pos
            pos = instPos
        }
        expect:
        pos == 100
    }

    def "Check custom installer position correct"() {

        expect:
        info.installers[1] == ManagedInstaller
        info.installers[2] == DummyInstaller
        info.installers[3] == JerseyProviderInstaller || info.installers[3] == JerseyFeatureInstaller

    }
}