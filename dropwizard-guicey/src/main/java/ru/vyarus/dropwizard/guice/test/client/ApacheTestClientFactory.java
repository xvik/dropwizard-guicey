package ru.vyarus.dropwizard.guice.test.client;

import io.dropwizard.testing.DropwizardTestSupport;
import org.glassfish.jersey.apache5.connector.Apache5ConnectorProvider;
import org.glassfish.jersey.client.JerseyClientBuilder;

/**
 * Use {@link Apache5ConnectorProvider} instead of default {@link org.glassfish.jersey.client.HttpUrlConnectorProvider}.
 *
 * @author Vyacheslav Rusakov
 * @since 12.09.2025
 */
public class ApacheTestClientFactory extends DefaultTestClientFactory {

    @Override
    protected void configure(final JerseyClientBuilder builder, final DropwizardTestSupport<?> support) {
        builder.getConfiguration().connectorProvider(new Apache5ConnectorProvider());
    }
}
