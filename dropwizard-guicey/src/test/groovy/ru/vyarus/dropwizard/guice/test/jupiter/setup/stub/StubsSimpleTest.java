package ru.vyarus.dropwizard.guice.test.jupiter.setup.stub;

import com.google.common.base.Preconditions;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import ru.vyarus.dropwizard.guice.support.DefaultTestApp;
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.stub.StubBean;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.stub.StubLifecycle;

/**
 * @author Vyacheslav Rusakov
 * @since 07.02.2025
 */
@TestGuiceyApp(value = DefaultTestApp.class, debug = true)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class StubsSimpleTest {

    @Inject
    Service1 service1;

    @Inject
    Service2 service2;

    @Inject
    Service service;

    @StubBean(Service1.class)
    Service1Stub stub;

    @StubBean(Service2.class)
    static Service2Stub stub2;

    @BeforeAll
    static void beforeAll() {
        Preconditions.checkNotNull(stub2);
    }

    @BeforeEach
    void setUp() {
        Preconditions.checkNotNull(stub);
        Preconditions.checkNotNull(stub2);
    }

    @Test
    @Order(1)
    void testStub() {
        Assertions.assertEquals(service1, stub);
        Assertions.assertEquals(service2, stub2);
        Assertions.assertEquals("moon", service.get());
        Assertions.assertTrue(stub.beforeCalled);
        Assertions.assertFalse(stub.afterCalled);
    }

    @Test
    @Order(2)
    void testStub2() {
        Assertions.assertEquals(service1, stub);
        Assertions.assertEquals(service2, stub2);
        Assertions.assertEquals("moon", service.get());
        Assertions.assertTrue(stub.beforeCalled);
        Assertions.assertTrue(stub.afterCalled);
    }

    public static class Service1Stub extends Service1 implements StubLifecycle {

        static boolean created;

        public boolean beforeCalled;
        public boolean afterCalled;

        public Service1Stub() {
            Preconditions.checkState(!created);
            created = true;
        }

        @Override
        public String get() {
            return "moon";
        }

        @Override
        public void before() {
            beforeCalled = true;
        }

        @Override
        public void after() {
            afterCalled = true;
        }
    }

    public static class Service2Stub extends Service2 {}

    public static class Service1 {

        public String get() {
            return "sun";
        }
    }

    public static class Service2 {
    }

    @Singleton
    public static class Service {

        @Inject
        Service1 service1;

        public String get() {
            return service1.get();
        }
    }
}
