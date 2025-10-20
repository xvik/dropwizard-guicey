package ru.vyarus.dropwizard.guice.test.jupiter.dw;

import org.glassfish.jersey.apache5.connector.Apache5ConnectorProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.vyarus.dropwizard.guice.support.AutoScanApplication;
import ru.vyarus.dropwizard.guice.test.ClientSupport;
import ru.vyarus.dropwizard.guice.test.jupiter.TestDropwizardApp;

/**
 * @author Vyacheslav Rusakov
 * @since 12.09.2025
 */
@TestDropwizardApp(value = AutoScanApplication.class, apacheClient = true)
public class ApacheClientFactoryDwTest {

    @Test
    void testApacheClientConfiguration(ClientSupport client) {
        Assertions.assertEquals(Apache5ConnectorProvider.class,
                client.getClient().getConfiguration().getConnectorProvider().getClass());
    }
}
