package ru.vyarus.dropwizard.guice.test.jupiter.setup.rest;

import jakarta.ws.rs.WebApplicationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp;
import ru.vyarus.dropwizard.guice.test.rest.RestClient;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.rest.StubRest;
import ru.vyarus.dropwizard.guice.test.rest.support.RestStubApp;

/**
 * @author Vyacheslav Rusakov
 * @since 25.02.2025
 */
@TestGuiceyApp(RestStubApp.class)
public class RestClientTest {

    @StubRest(disableDropwizardExceptionMappers = true)
    RestClient rest;

    @Test
    void testClient() {
        Assertions.assertNotNull(rest.client());
        Assertions.assertNotNull(rest.getBaseUri());
        Assertions.assertEquals(0, rest.getBaseUri().getPort());

        Assertions.assertFalse(rest.hasDefaultAccepts());
        Assertions.assertFalse(rest.hasDefaultHeaders());
        Assertions.assertFalse(rest.hasDefaultQueryParams());
        Assertions.assertFalse(rest.isDefaultStatusChanged());

        String res = rest.target("/1/foo").request().get(String.class);
        Assertions.assertEquals("foo", res);

        res = rest.get("/1/bar", String.class);
        Assertions.assertEquals("bar", res);

        rest.post("/1/foo", null, null);
        res = rest.put("/1/foo", "something", String.class);
        Assertions.assertEquals("foo", res);
        rest.delete("/1/bar", null);
    }

    @Test
    void testErrors() {
        WebApplicationException ex = Assertions.assertThrows(WebApplicationException.class, () -> rest.get("/error/foo", String.class));
        Assertions.assertEquals("error", ex.getResponse().readEntity(String.class));

        ex = Assertions.assertThrows(WebApplicationException.class, () -> rest.post("/error/foo", null, String.class));
        Assertions.assertEquals("error", ex.getResponse().readEntity(String.class));

        ex = Assertions.assertThrows(WebApplicationException.class, () -> rest.put("/error/foo", "something", String.class));
        Assertions.assertEquals("error", ex.getResponse().readEntity(String.class));

        ex = Assertions.assertThrows(WebApplicationException.class, () -> rest.delete("/error/foo", String.class));
        Assertions.assertEquals("error", ex.getResponse().readEntity(String.class));
    }

    @Test
    void testSuccessVoidCalls() {
        Assertions.assertNull(rest.get("/1/foo", null));
        Assertions.assertNull(rest.post("/1/foo", null, null));
        Assertions.assertNull(rest.put("/1/foo", "something", null));
        Assertions.assertNull(rest.delete("/1/foo", null));
    }

    @Test
    void testVoidStatusCheck() {
        rest.defaultOk(500);
        Assertions.assertTrue(rest.isDefaultStatusChanged());

        IllegalStateException ex = Assertions.assertThrows(IllegalStateException.class, () -> rest.get("/1/foo", null));
        Assertions.assertTrue(ex.getMessage().contains("Invalid response: 200"));

        ex = Assertions.assertThrows(IllegalStateException.class, () -> rest.post("/1/foo", null, null));
        Assertions.assertTrue(ex.getMessage().contains("Invalid response: 204"));

        ex = Assertions.assertThrows(IllegalStateException.class, () -> rest.put("/1/foo", "something", null));
        Assertions.assertTrue(ex.getMessage().contains("Invalid response: 200"));

        ex = Assertions.assertThrows(IllegalStateException.class, () -> rest.delete("/1/foo", null));
        Assertions.assertTrue(ex.getMessage().contains("Invalid response: 204"));
    }
}
