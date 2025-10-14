package ru.vyarus.dropwizard.guice.test.jupiter.setup.rest;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.client.Entity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;
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

        String res = rest.target("/1/foo").request().get(String.class);
        Assertions.assertEquals("foo", res);

        res = rest.get("/1/bar", String.class);
        Assertions.assertEquals("bar", res);

        rest.post("/1/foo", null);
        res = rest.put("/1/foo", "something", String.class);
        Assertions.assertEquals("foo", res);

        // patch body can't be null
        rest.patch("/1/foo", Entity.text("sample"));
        res = rest.patch("/1/foo", "something", String.class);
        Assertions.assertEquals("foo", res);

        rest.delete("/1/bar");
    }

    @Test
    void testErrors() {
        WebApplicationException ex = Assertions.assertThrows(WebApplicationException.class, () -> rest.get("/error/foo", String.class));
        Assertions.assertEquals("error", ex.getResponse().readEntity(String.class));

        ex = Assertions.assertThrows(WebApplicationException.class, () -> rest.post("/error/foo", null, String.class));
        Assertions.assertEquals("error", ex.getResponse().readEntity(String.class));

        ex = Assertions.assertThrows(WebApplicationException.class, () -> rest.put("/error/foo", "something", String.class));
        Assertions.assertEquals("error", ex.getResponse().readEntity(String.class));

        ex = Assertions.assertThrows(WebApplicationException.class, () -> rest.patch("/error/foo", "something", String.class));
        Assertions.assertEquals("error", ex.getResponse().readEntity(String.class));

        ex = Assertions.assertThrows(WebApplicationException.class, () -> rest.delete("/error/foo", String.class));
        Assertions.assertEquals("error", ex.getResponse().readEntity(String.class));
    }

    @Test
    void testSuccessVoidCalls() {
        Assertions.assertNull(rest.get("/1/foo", Void.class));
        Assertions.assertNull(rest.post("/1/foo", null, Void.class));
        Assertions.assertNull(rest.put("/1/foo", "something", Void.class));
        Assertions.assertNull(rest.delete("/1/foo", Void.class));
    }

    @Test
    void testVoidStatusCheck() {
        AssertionFailedError ex = Assertions.assertThrows(AssertionFailedError.class, () ->
                rest.buildGet("/1/foo").expectSuccess(205));
        Assertions.assertTrue(ex.getMessage().contains("Unexpected response status 200"));

        ex = Assertions.assertThrows(AssertionFailedError.class, () ->
                rest.buildPost("/1/foo", null).expectSuccess(205));
        Assertions.assertTrue(ex.getMessage().contains("Unexpected response status 204"));

        ex = Assertions.assertThrows(AssertionFailedError.class, () ->
                rest.buildPut("/1/foo", "something").expectSuccess(205));
        Assertions.assertTrue(ex.getMessage().contains("Unexpected response status 200"));

        ex = Assertions.assertThrows(AssertionFailedError.class, () ->
                rest.buildDelete("/1/foo").expectSuccess(205));
        Assertions.assertTrue(ex.getMessage().contains("Unexpected response status 204"));
    }
}
