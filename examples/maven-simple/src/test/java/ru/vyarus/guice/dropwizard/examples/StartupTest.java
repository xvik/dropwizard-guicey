package ru.vyarus.guice.dropwizard.examples;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.vyarus.dropwizard.guice.test.ClientSupport;
import ru.vyarus.dropwizard.guice.test.jupiter.TestDropwizardApp;

/**
 * @author Vyacheslav Rusakov
 * @since 03.07.2023
 */
@TestDropwizardApp(SampleApplication.class)
public class StartupTest {

    @Test
    void checkStartup(ClientSupport client) {

        String res = client.targetRest("/sample").request().get().readEntity(String.class);
        Assertions.assertEquals("127.0.0.1", res);
    }
}
