package ru.vyarus.dropwizard.guice.test.client;

import io.dropwizard.testing.DropwizardTestSupport;
import org.glassfish.jersey.client.JerseyClient;

/**
 * Factory for {@link org.glassfish.jersey.client.JerseyClient} instance creation for
 * {@link ru.vyarus.dropwizard.guice.test.ClientSupport}.
 * <p>
 * Custom factory might be useful, for example, if multipart data support should be enabled or gzip decoding.
 *
 * @author Vyacheslav Rusakov
 * @since 15.11.2023
 */
@FunctionalInterface
public interface TestClientFactory {

    /**
     * Creates client instance for{@link ru.vyarus.dropwizard.guice.test.ClientSupport} (once per support instance).
     * Called lazily (only before jersey client is actually required to perform call), so the support object should be
     * already initialized.
     *
     * @param support support object
     * @return client instance
     */
    JerseyClient create(DropwizardTestSupport<?> support);
}
