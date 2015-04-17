package ru.vyarus.dropwizard.guice.order

import com.google.inject.Inject
import ru.vyarus.dropwizard.guice.AbstractTest
import ru.vyarus.dropwizard.guice.module.installer.feature.ManagedInstaller
import ru.vyarus.dropwizard.guice.module.installer.feature.jersey.provider.JerseyProviderInstaller
import ru.vyarus.dropwizard.guice.module.installer.internal.FeaturesHolder
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
    FeaturesHolder holder

    def "Check default installers order"() {

        def pos = 0;
        holder.getInstallers().each {
            int instPos = it.class.getAnnotation(Order).value()
            assert instPos > pos
            pos = instPos
        }
        expect:
        pos == 100
    }

    def "Check custom installer position correct"() {

        expect:
        holder.getInstallers()[1] instanceof ManagedInstaller
        holder.getInstallers()[2] instanceof DummyInstaller
        holder.getInstallers()[3] instanceof JerseyProviderInstaller

    }
}