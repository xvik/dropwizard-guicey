package ru.vyarus.dropwizard.guice.test.jupiter.guicey;

import org.glassfish.jersey.apache5.connector.Apache5ConnectorProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import ru.vyarus.dropwizard.guice.support.AutoScanApplication;
import ru.vyarus.dropwizard.guice.test.ClientSupport;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.TestGuiceyAppExtension;

/**
 * @author Vyacheslav Rusakov
 * @since 12.09.2025
 */
public class ManualRegApacheClientGuiceyTest {

    @RegisterExtension
    static TestGuiceyAppExtension app = TestGuiceyAppExtension.forApp(AutoScanApplication.class)
            .apacheClient()
            .create();

    @Test
    void testApacheClientRegistration(ClientSupport client) {
        Assertions.assertEquals(Apache5ConnectorProvider.class,
                client.getClient().getConfiguration().getConnectorProvider().getClass());
    }
}
