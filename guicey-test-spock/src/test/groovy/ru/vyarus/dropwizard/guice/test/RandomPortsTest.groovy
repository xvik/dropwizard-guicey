package ru.vyarus.dropwizard.guice.test

import io.dropwizard.core.Configuration
import io.dropwizard.jetty.HttpConnectorFactory
import io.dropwizard.core.server.DefaultServerFactory
import ru.vyarus.dropwizard.guice.support.AutoScanApplication
import ru.vyarus.dropwizard.guice.test.spock.UseDropwizardApp
import spock.lang.Specification

import javax.inject.Inject

/**
 * @author Vyacheslav Rusakov
 * @since 17.05.2020
 */
@UseDropwizardApp(value = AutoScanApplication.class, randomPorts = true)
class RandomPortsTest extends Specification {

    @Inject
    Configuration config

    def "Check random ports"() {

        setup:
        DefaultServerFactory factory = (DefaultServerFactory) config.getServerFactory()

        expect: "random ports applied"
        ((HttpConnectorFactory) factory.getApplicationConnectors().first()).getPort() == 0
        ((HttpConnectorFactory) factory.getAdminConnectors().first()).getPort() == 0
    }
}
