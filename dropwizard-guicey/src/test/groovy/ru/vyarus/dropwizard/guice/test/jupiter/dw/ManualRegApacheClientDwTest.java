package ru.vyarus.dropwizard.guice.test.jupiter.dw;

import org.glassfish.jersey.apache5.connector.Apache5ConnectorProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import ru.vyarus.dropwizard.guice.support.AutoScanApplication;
import ru.vyarus.dropwizard.guice.test.ClientSupport;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.TestDropwizardAppExtension;

/**
 * @author Vyacheslav Rusakov
 * @since 12.09.2025
 */
public class ManualRegApacheClientDwTest {

    @RegisterExtension
    static TestDropwizardAppExtension app = TestDropwizardAppExtension.forApp(AutoScanApplication.class)
            .randomPorts()
            .useApacheClient()
            .create();

    @Test
    void testApacheClientConfiguration(ClientSupport client) {
        Assertions.assertEquals(Apache5ConnectorProvider.class,
                client.getClient().getConfiguration().getConnectorProvider().getClass());
    }
}
