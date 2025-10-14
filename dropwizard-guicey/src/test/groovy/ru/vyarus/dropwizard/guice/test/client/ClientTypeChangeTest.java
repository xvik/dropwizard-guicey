package ru.vyarus.dropwizard.guice.test.client;

import org.glassfish.jersey.apache5.connector.Apache5ConnectorProvider;
import org.glassfish.jersey.client.HttpUrlConnectorProvider;
import org.junit.jupiter.api.Test;
import ru.vyarus.dropwizard.guice.test.ClientSupport;
import ru.vyarus.dropwizard.guice.test.client.support.ClientApp;
import ru.vyarus.dropwizard.guice.test.jupiter.TestDropwizardApp;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Vyacheslav Rusakov
 * @since 13.10.2025
 */
@TestDropwizardApp(ClientApp.class)
public class ClientTypeChangeTest {

    @Test
    void testUrlconnectionClientFailure(ClientSupport client) {
        ClientSupport support = client.urlconnectorClient();
        assertThat(support.getClient().getConfiguration().getConnectorProvider().getClass())
                .isEqualTo(HttpUrlConnectorProvider.class);

        support = client.apacheClient();
        assertThat(support.getClient().getConfiguration().getConnectorProvider().getClass())
                .isEqualTo(Apache5ConnectorProvider.class);
    }
}
