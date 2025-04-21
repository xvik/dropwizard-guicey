package ru.vyarus.dropwizard.guice.test.jupiter.setup.rest;

import com.google.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import ru.vyarus.dropwizard.guice.hook.GuiceyConfigurationHook;
import ru.vyarus.dropwizard.guice.test.EnableHook;
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.mock.MockBean;
import ru.vyarus.dropwizard.guice.test.rest.RestClient;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.rest.StubRest;
import ru.vyarus.dropwizard.guice.test.rest.support.RestStubApp;

/**
 * @author Vyacheslav Rusakov
 * @since 25.02.2025
 */
@TestGuiceyApp(RestStubApp.class)
public class MocksCompatibilityTest {

    @EnableHook
    static GuiceyConfigurationHook hook = builder -> builder.extensions(TestResource.class);

    @StubRest(TestResource.class)
    RestClient rest;

    @MockBean
    Service service;

    @Test
    void testMocksSupport() {
        Mockito.when(service.foo()).thenReturn("bar");

        String res = rest.get("/test/", String.class);
        Assertions.assertEquals("bar", res);
    }

    @Path("/test/")
    public static class TestResource {
        @Inject
        Service service;

        @GET
        @Path("/")
        public String get() {
            return service.foo();
        }
    }

    public static class Service {

        public String foo() {
            return "foo";
        }
    }
}
