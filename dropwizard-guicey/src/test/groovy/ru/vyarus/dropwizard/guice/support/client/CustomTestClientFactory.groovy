package ru.vyarus.dropwizard.guice.support.client

import io.dropwizard.testing.DropwizardTestSupport
import org.glassfish.jersey.client.JerseyClient
import ru.vyarus.dropwizard.guice.test.client.DefaultTestClientFactory

/**
 * @author Vyacheslav Rusakov
 * @since 16.11.2023
 */
class CustomTestClientFactory extends DefaultTestClientFactory {

    // not assumed to be used in concurrent tests
    static int called

    CustomTestClientFactory() {
        called = 0
    }

    @Override
    JerseyClient create(DropwizardTestSupport<?> support) {
        called++
        return super.create(support)
    }
}
