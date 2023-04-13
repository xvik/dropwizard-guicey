package ru.vyarus.dropwizard.guice.test.jupiter.dw;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.vyarus.dropwizard.guice.support.AutoScanApplication;
import ru.vyarus.dropwizard.guice.test.jupiter.TestDropwizardApp;

import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.Response;

/**
 * @author Vyacheslav Rusakov
 * @since 02.05.2020
 */
@TestDropwizardApp(value = AutoScanApplication.class, restMapping = "api")
public class CustomRestMappingTest {

    @Test
    void checkRestMapped() {
        Response response = ClientBuilder.newClient()
                .target("http://localhost:8080/api/dummy/")
                .request()
                .buildGet()
                .invoke();

        Assertions.assertEquals(200, response.getStatus());
    }
}
