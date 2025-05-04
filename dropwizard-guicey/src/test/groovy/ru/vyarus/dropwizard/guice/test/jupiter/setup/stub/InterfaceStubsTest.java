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
import ru.vyarus.dropwizard.guice.GuiceBundle;
import ru.vyarus.dropwizard.guice.support.DefaultTestApp;
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.stub.StubBean;
import ru.vyarus.dropwizard.guice.test.stub.StubLifecycle;

/**
 * @author Vyacheslav Rusakov
 * @since 08.02.2025
 */
@TestGuiceyApp(value = InterfaceStubsTest.App.class, debug = true)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class InterfaceStubsTest {

    @Inject
    IService1 service1;

    @Inject
    IService2 service2;

    @Inject
    Service service;

    @StubBean(IService1.class)
    Service1Stub stub;

    @StubBean(IService2.class)
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

    public static class App extends DefaultTestApp {

        @Override
        protected GuiceBundle configure() {
            return GuiceBundle.builder()
                    .modules(binder -> {
                        binder.bind(IService1.class).to(Service1Impl.class);
                        binder.bind(IService2.class).to(Service2Impl.class);
                    })
                    .build();
        }
    }

    public static class Service1Stub implements IService1, StubLifecycle {

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

    public static class Service2Stub implements IService2 {}

    public interface IService1 {
        String get();
    }
    public static class Service1Impl implements IService1 {

        @Override
        public String get() {
            return "sun";
        }
    }


    public interface IService2 {}
    public static class Service2Impl implements IService2 {
    }

    @Singleton
    public static class Service {

        @Inject
        IService1 service1;

        public String get() {
            return service1.get();
        }
    }
}
