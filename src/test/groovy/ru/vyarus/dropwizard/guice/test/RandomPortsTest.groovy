package ru.vyarus.dropwizard.guice.test

import io.dropwizard.Configuration
import io.dropwizard.jetty.HttpConnectorFactory
import io.dropwizard.server.DefaultServerFactory
import ru.vyarus.dropwizard.guice.AbstractTest
import ru.vyarus.dropwizard.guice.support.AutoScanApplication
import ru.vyarus.dropwizard.guice.test.spock.UseDropwizardApp

import javax.inject.Inject

/**
 * @author Vyacheslav Rusakov
 * @since 17.05.2020
 */
@UseDropwizardApp(value = AutoScanApplication.class, randomPorts = true)
class RandomPortsTest extends AbstractTest {

    @Inject
    Configuration config

    def "Check random ports"() {

        expect: "random ports applied"
        ((HttpConnectorFactory)
                ((DefaultServerFactory) config.getServerFactory()).getApplicationConnectors().first())
                .getPort() == 0
        ((HttpConnectorFactory)
                ((DefaultServerFactory) config.getServerFactory()).getAdminConnectors().first())
                .getPort() == 0
    }
}
