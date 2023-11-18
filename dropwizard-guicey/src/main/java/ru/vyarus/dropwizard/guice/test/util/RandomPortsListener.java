package ru.vyarus.dropwizard.guice.test.util;

import io.dropwizard.Configuration;
import io.dropwizard.server.DefaultServerFactory;
import io.dropwizard.server.ServerFactory;
import io.dropwizard.server.SimpleServerFactory;
import io.dropwizard.setup.Environment;
import io.dropwizard.jetty.HttpConnectorFactory;
import io.dropwizard.testing.DropwizardTestSupport;

/**
 * Applies random ports to test application.
 *
 * @param <C> configuration type
 */
public class RandomPortsListener<C extends Configuration> extends DropwizardTestSupport.ServiceListener<C> {
    @Override
    public void onRun(final C configuration,
                      final Environment environment,
                      final DropwizardTestSupport<C> rule) throws Exception {
        final ServerFactory server = configuration.getServerFactory();
        if (server instanceof SimpleServerFactory) {
            ((HttpConnectorFactory) ((SimpleServerFactory) server).getConnector()).setPort(0);
        } else {
            final DefaultServerFactory dserv = (DefaultServerFactory) server;
            ((HttpConnectorFactory) dserv.getApplicationConnectors().get(0)).setPort(0);
            ((HttpConnectorFactory) dserv.getAdminConnectors().get(0)).setPort(0);
        }
    }
}
