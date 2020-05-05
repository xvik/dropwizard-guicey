package ru.vyarus.dropwizard.guice.test.jupiter.dw;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.vyarus.dropwizard.guice.support.AutoScanApplication;
import ru.vyarus.dropwizard.guice.test.jupiter.TestDropwizardApp;
import ru.vyarus.dropwizard.guice.test.jupiter.param.ClientSupport;

/**
 * @author Vyacheslav Rusakov
 * @since 01.05.2020
 */
@TestDropwizardApp(value = AutoScanApplication.class, randomPorts = true)
public class RandomPortsTest {

    @Test
    void checkRandomPorts(ClientSupport client) {
        Assertions.assertNotEquals(8080, client.getPort());
        Assertions.assertNotEquals(8081, client.getAdminPort());
    }
}
