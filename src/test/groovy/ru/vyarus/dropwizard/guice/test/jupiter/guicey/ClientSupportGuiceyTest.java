package ru.vyarus.dropwizard.guice.test.jupiter.guicey;

import io.dropwizard.testing.junit5.DropwizardAppExtension;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import ru.vyarus.dropwizard.guice.support.AutoScanApplication;
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp;
import ru.vyarus.dropwizard.guice.test.jupiter.param.ClientSupport;

/**
 * @author Vyacheslav Rusakov
 * @since 05.05.2020
 */
@TestGuiceyApp(AutoScanApplication.class)
@ExtendWith(DropwizardExtensionsSupport.class)
public class ClientSupportGuiceyTest {

    // use dropwizard extension to start separate server
    // NOTE guicey will start DIFFERENT application
    static DropwizardAppExtension app = new DropwizardAppExtension(AutoScanApplication.class);

    @Test
    void testLimitedClient(ClientSupport client) {
        Assertions.assertEquals(200, client.target("http://localhost:8080/dummy/")
                .request().buildGet().invoke().getStatus());

        // web methods obviously doesnt work
        Assertions.assertThrows(NullPointerException.class, client::basePathMain);
    }
}
