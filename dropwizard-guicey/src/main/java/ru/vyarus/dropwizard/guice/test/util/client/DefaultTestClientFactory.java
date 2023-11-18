package ru.vyarus.dropwizard.guice.test.util.client;

import io.dropwizard.jersey.jackson.JacksonFeature;
import io.dropwizard.testing.DropwizardTestSupport;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.HttpUrlConnectorProvider;
import org.glassfish.jersey.client.JerseyClient;
import org.glassfish.jersey.client.JerseyClientBuilder;

/**
 * Default client factory for {@link ru.vyarus.dropwizard.guice.test.ClientSupport}. Auto register multipart
 * feature if it's available in classpath (through dropwizard-froms).
 *
 * @author Vyacheslav Rusakov
 * @since 15.11.2023
 */
public class DefaultTestClientFactory implements TestClientFactory {

    @Override
    public JerseyClient create(final DropwizardTestSupport<?> support) {
        final JerseyClientBuilder builder = new JerseyClientBuilder()
                .register(new JacksonFeature(support.getEnvironment().getObjectMapper()))
                .property(ClientProperties.CONNECT_TIMEOUT, 1000)
                .property(ClientProperties.READ_TIMEOUT, 5000)
                .property(HttpUrlConnectorProvider.SET_METHOD_WORKAROUND, true);
        try {
            // when dropwizard-forms used automatically register multipart feature
            final Class<?> cls = Class.forName("org.glassfish.jersey.media.multipart.MultiPartFeature");
            builder.register(cls);
        } catch (Exception ignored) {
            // do nothing - no multipart feature available
        }
        return builder.build();
    }
}
