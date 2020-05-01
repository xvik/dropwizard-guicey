package ru.vyarus.dropwizard.guice.test.jupiter.dw;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.vyarus.dropwizard.guice.support.AutoScanApplication;
import ru.vyarus.dropwizard.guice.test.jupiter.TestDropwizardApp;
import ru.vyarus.dropwizard.guice.test.jupiter.param.AppAdminPort;
import ru.vyarus.dropwizard.guice.test.jupiter.param.AppPort;

/**
 * @author Vyacheslav Rusakov
 * @since 01.05.2020
 */
@TestDropwizardApp(value = AutoScanApplication.class, randomPorts = true)
public class RandomPortsTest {

    @Test
    void checkRandomPorts(@AppPort int port, @AppAdminPort int adminPort) {
        Assertions.assertNotEquals(8080, port);
        Assertions.assertNotEquals(8081, adminPort);
    }
}
