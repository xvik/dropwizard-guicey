package ru.vyarus.dropwizard.guice.test.jupiter.setup.client;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.vyarus.dropwizard.guice.support.DefaultTestApp;
import ru.vyarus.dropwizard.guice.test.ClientSupport;
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.client.WebClient;

/**
 * @author Vyacheslav Rusakov
 * @since 17.10.2025
 */
@TestGuiceyApp(DefaultTestApp.class)
public class GuiceyTestClientInjectionTest {

    @WebClient
    ClientSupport client;

    @Test
    void testClientInjected(ClientSupport client) {
        Assertions.assertThat(client).isNotNull().isSameAs(this.client);
    }
}
