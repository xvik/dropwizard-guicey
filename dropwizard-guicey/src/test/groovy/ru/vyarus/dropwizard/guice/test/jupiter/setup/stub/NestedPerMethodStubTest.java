package ru.vyarus.dropwizard.guice.test.jupiter.setup.stub;

import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import ru.vyarus.dropwizard.guice.support.DefaultTestApp;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.TestGuiceyAppExtension;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.stub.StubBean;

/**
 * @author Vyacheslav Rusakov
 * @since 09.02.2025
 */
public class NestedPerMethodStubTest {

    @RegisterExtension
    TestGuiceyAppExtension ext = TestGuiceyAppExtension.forApp(DefaultTestApp.class)
            .debug().create();

    @StubBean(Service.class)
    ServiceStub stub;

    @StubBean(Service2.class)
    static Service2Stub stub2 = new Service2Stub("manual");

    @Test
    void testStub() {
        Assertions.assertNotNull(stub);
        Assertions.assertNotNull(stub2);
    }

    @Nested
    class Inner {

        @Inject
        Service service;

        @Inject
        Service2 service2;

        @Inject
        Service3 service3;

        @Inject
        Service4 service4;

        @StubBean(Service3.class)
        Service3Stub stub3;

        @Test
        void testStubUsed() {
            Assertions.assertNotNull(stub);
            Assertions.assertNotNull(stub2);
            Assertions.assertNotNull(stub3);
            Assertions.assertEquals(service, stub);
            Assertions.assertEquals("bar", service.foo());
            Assertions.assertEquals(service2, stub2);
            Assertions.assertEquals("bar", service2.foo());
            Assertions.assertEquals(service3, stub3);
            Assertions.assertEquals("bar", service3.foo());
        }
    }

    public static class Service {
        public String foo() {
            return "foo";
        }
    }
    public static class ServiceStub extends Service {
        @Override
        public String foo() {
            return "bar";
        }
    }

    public static class Service2 {
        public String foo() {
            return "foo";
        }
    }
    public static class Service2Stub extends Service2 {

        public Service2Stub(String custom) {
        }

        @Override
        public String foo() {
            return "bar";
        }
    }

    public static class Service3 {
        public String foo() {
            return "foo";
        }
    }
    public static class Service3Stub extends Service3 {
        @Override
        public String foo() {
            return "bar";
        }
    }

    public static class Service4 {
        public String foo() {
            return "foo";
        }
    }
    public static class Service4Stub extends Service4 {

        public Service4Stub(String custom) {
        }

        @Override
        public String foo() {
            return "bar";
        }
    }
}
