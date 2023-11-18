package ru.vyarus.dropwizard.guice.test.jupiter.dw;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.vyarus.dropwizard.guice.support.AutoScanApplication;
import ru.vyarus.dropwizard.guice.support.client.CustomTestClientFactory;
import ru.vyarus.dropwizard.guice.test.ClientSupport;
import ru.vyarus.dropwizard.guice.test.jupiter.TestDropwizardApp;

/**
 * @author Vyacheslav Rusakov
 * @since 16.11.2023
 */
@TestDropwizardApp(value = AutoScanApplication.class, clientFactory = CustomTestClientFactory.class)
public class CustomClientFactoryDwTest {

    @Test
    void testCustomClientFactory(ClientSupport client) {
        Assertions.assertEquals(0, CustomTestClientFactory.getCalled());
        client.getClient(); // force client creation
        Assertions.assertEquals(1, CustomTestClientFactory.getCalled());
    }
}
