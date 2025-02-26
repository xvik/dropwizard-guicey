package ru.vyarus.dropwizard.guice.test.jupiter.setup.rest;

import jakarta.ws.rs.WebApplicationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import ru.vyarus.dropwizard.guice.AbstractPlatformTest;
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.rest.RestClient;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.rest.StubRest;
import ru.vyarus.dropwizard.guice.test.jupiter.setup.rest.support.RestStubApp;

/**
 * @author Vyacheslav Rusakov
 * @since 24.02.2025
 */

public class DropwizardExtensionsDisableTest extends AbstractPlatformTest {

    @Test
    void testDwMappersDisable() {

        String out = run(Test1.class);
        Assertions.assertTrue(out.contains(">>>ERROR:\nerror"));

        out = run(Test2.class);
        Assertions.assertTrue(out.contains(">>>ERROR:\n{\"code\":500,\"message\":\"There was an error processing your request. It has been logged"));
    }

    @TestGuiceyApp(RestStubApp.class)
    @Disabled
    public static class Test1 {

        @StubRest(disableDropwizardExceptionMappers = true)
        RestClient rest;

        @Test
        void testMappersDisabled() {
            final WebApplicationException ex = Assertions.assertThrows(WebApplicationException.class,
                    () -> rest.get("/error/1", String.class));

            System.out.println(">>>ERROR:\n" + ex.getResponse().readEntity(String.class));
        }
    }

    @TestGuiceyApp(RestStubApp.class)
    @Disabled
    public static class Test2 {

        @StubRest
        RestClient rest;

        @Test
        void testMappersDisabled() {
            final WebApplicationException ex = Assertions.assertThrows(WebApplicationException.class,
                    () -> rest.get("/error/1", String.class));

            System.out.println(">>>ERROR:\n" + ex.getResponse().readEntity(String.class));
        }
    }

    @Override
    protected String clean(String out) {
        return out;
    }
}
