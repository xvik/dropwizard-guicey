package ru.vyarus.dropwizard.guice.test.jupiter.setup.stub;

import com.google.common.base.Preconditions;
import io.dropwizard.core.Application;
import io.dropwizard.core.Configuration;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import ru.vyarus.dropwizard.guice.GuiceBundle;
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.stub.StubBean;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.stub.StubLifecycle;

/**
 * @author Vyacheslav Rusakov
 * @since 09.02.2025
 */
@TestGuiceyApp(value = ManualStubsTest.App.class, debug = true)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ManualStubsTest {
    @Inject
    Service1 service1;

    @Inject
    Service2 service2;

    @Inject
    Service service;

    @StubBean(Service1.class)
    Service1Stub stub = new Service1Stub("manual");

    @StubBean(Service2.class)
    static Service2Stub stub2 = new Service2Stub("manual");

    @BeforeAll
    static void beforeAll() {
        Preconditions.checkNotNull(stub2);
        Assertions.assertEquals("manual", stub2.source);
    }

    @BeforeEach
    void setUp() {
        Preconditions.checkNotNull(stub);
        Assertions.assertEquals("manual", stub.source);
        Preconditions.checkNotNull(stub2);
        Assertions.assertEquals("manual", stub2.source);
    }

    @Test
    @Order(1)
    void testStub() {
        Assertions.assertEquals(service1, stub);
        Assertions.assertEquals(service2, stub2);
        Assertions.assertEquals("manual", stub.source);
        Assertions.assertEquals("manual", stub2.source);
        Assertions.assertEquals("moon", service.get());
        Assertions.assertTrue(stub.beforeCalled);
        Assertions.assertFalse(stub.afterCalled);
    }

    @Test
    @Order(2)
    void testStub2() {
        Assertions.assertEquals(service1, stub);
        Assertions.assertEquals(service2, stub2);
        Assertions.assertEquals("manual", stub.source);
        Assertions.assertEquals("manual", stub2.source);
        Assertions.assertEquals("moon", service.get());
        Assertions.assertTrue(stub.beforeCalled);
        Assertions.assertTrue(stub.afterCalled);
    }

    public static class App extends Application<Configuration> {

        @Override
        public void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder().build());
        }

        @Override
        public void run(Configuration configuration, Environment environment) throws Exception {
        }
    }

    public static class Service1Stub extends Service1 implements StubLifecycle {

        static boolean created;

        public boolean beforeCalled;
        public boolean afterCalled;

        public String source;

        public Service1Stub(String source) {
            Preconditions.checkState(!created);
            created = true;
            this.source = source;
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

    public static class Service2Stub extends Service2 {
        public String source;

        public Service2Stub(String source) {
            this.source = source;
        }
    }

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
