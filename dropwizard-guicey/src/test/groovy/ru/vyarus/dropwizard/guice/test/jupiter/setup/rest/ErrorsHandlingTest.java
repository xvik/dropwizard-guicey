package ru.vyarus.dropwizard.guice.test.jupiter.setup.rest;

import jakarta.ws.rs.ProcessingException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.rest.StubRest;
import ru.vyarus.dropwizard.guice.test.rest.RestClient;
import ru.vyarus.dropwizard.guice.test.rest.support.RestStubApp;

/**
 * @author Vyacheslav Rusakov
 * @since 19.09.2025
 */
@TestGuiceyApp(RestStubApp.class)
public class ErrorsHandlingTest {

    // disable all exception mappers to receive raw exceptions
    @StubRest(disableDropwizardExceptionMappers = true, disableAllJerseyExtensions = true)
    RestClient rest;

    @Test
    void testErrorPropagation() {
        ProcessingException ex = Assertions.assertThrows(ProcessingException.class, () ->
                rest.get("/error/foo", String.class));
        Assertions.assertEquals("error", ex.getCause().getMessage());
    }

    @Test
    void testErrors() {
        ProcessingException ex = Assertions.assertThrows(ProcessingException.class, () -> rest.get("/error/foo", String.class));
        Assertions.assertEquals("error", ex.getCause().getMessage());

        ex = Assertions.assertThrows(ProcessingException.class, () -> rest.post("/error/foo", null, String.class));
        Assertions.assertEquals("error", ex.getCause().getMessage());

        ex = Assertions.assertThrows(ProcessingException.class, () -> rest.put("/error/foo", "something", String.class));
        Assertions.assertEquals("error", ex.getCause().getMessage());

        ex = Assertions.assertThrows(ProcessingException.class, () -> rest.patch("/error/foo", "something", String.class));
        Assertions.assertEquals("error", ex.getCause().getMessage());

        ex = Assertions.assertThrows(ProcessingException.class, () -> rest.delete("/error/foo", String.class));
        Assertions.assertEquals("error", ex.getCause().getMessage());
    }

}
