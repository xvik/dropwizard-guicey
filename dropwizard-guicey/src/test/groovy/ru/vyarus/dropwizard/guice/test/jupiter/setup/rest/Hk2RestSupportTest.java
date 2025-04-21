package ru.vyarus.dropwizard.guice.test.jupiter.setup.rest;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.vyarus.dropwizard.guice.hook.GuiceyConfigurationHook;
import ru.vyarus.dropwizard.guice.module.installer.feature.jersey.JerseyManaged;
import ru.vyarus.dropwizard.guice.test.EnableHook;
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp;
import ru.vyarus.dropwizard.guice.test.rest.RestClient;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.rest.StubRest;
import ru.vyarus.dropwizard.guice.test.rest.support.RestStubApp;

/**
 * @author Vyacheslav Rusakov
 * @since 24.02.2025
 */
@TestGuiceyApp(RestStubApp.class)
public class Hk2RestSupportTest {

    @EnableHook
    static GuiceyConfigurationHook hook = builder -> builder.extensions(HkResource.class);

    @StubRest(value = HkResource.class, logRequests = true)
    RestClient rest;

    @Test
    void testHkRest() {

        String res = rest.get("/hk/", String.class);
        Assertions.assertEquals(res, "foo");
    }

    @Path("/hk/")
    @JerseyManaged
    public static class HkResource {

        @GET
        public String foo() {
            return "foo";
        }
    }
}
