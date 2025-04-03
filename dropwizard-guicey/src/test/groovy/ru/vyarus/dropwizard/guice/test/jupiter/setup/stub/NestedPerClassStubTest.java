package ru.vyarus.dropwizard.guice.test.jupiter.setup.stub;

import javax.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import ru.vyarus.dropwizard.guice.support.DefaultTestApp;
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.stub.StubBean;

/**
 * @author Vyacheslav Rusakov
 * @since 09.02.2025
 */
@TestGuiceyApp(DefaultTestApp.class)
public class NestedPerClassStubTest {

    @StubBean(Service.class)
    ServiceStub stub;

    @Test
    void testStub() {
        Assertions.assertNotNull(stub);
    }

    @Nested
    class Inner {

        @Inject
        Service service;

        @Test
        void testStubUsed() {
            Assertions.assertEquals(service, stub);
        }
    }

    public static class Service {}

    public static class ServiceStub extends Service {}
}
