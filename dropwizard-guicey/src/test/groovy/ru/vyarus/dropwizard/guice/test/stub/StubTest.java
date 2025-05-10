package ru.vyarus.dropwizard.guice.test.stub;

import com.google.common.base.Preconditions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.vyarus.dropwizard.guice.support.DefaultTestApp;
import ru.vyarus.dropwizard.guice.test.TestSupport;

/**
 * @author Vyacheslav Rusakov
 * @since 04.05.2025
 */
public class StubTest {

    @BeforeEach
    void setUp() {
        Service1Stub.created = false;
    }

    @Test
    void testStubs() throws Exception {
        StubsHook hook = new StubsHook();
        hook.stub(Service1.class, Service1Stub.class);

        TestSupport.build(DefaultTestApp.class)
                .hooks(hook)
                .runCore(injector -> {
                    Service1 service1 = injector.getInstance(Service1.class);

                    Assertions.assertInstanceOf(Service1Stub.class, service1);
                    Assertions.assertEquals("moon", service1.get());

                    Service1Stub stub = (Service1Stub) service1;
                    hook.before();
                    Assertions.assertTrue(stub.beforeCalled);
                    hook.after();
                    Assertions.assertTrue(stub.afterCalled);

                    Assertions.assertEquals(stub, hook.getStub(Service1.class));

                    return null;
                });
    }

    @Test
    void testManualStubs() throws Exception {
        StubsHook hook = new StubsHook();
        hook.stub(Service1.class, new Service1Stub());

        TestSupport.build(DefaultTestApp.class)
                .hooks(hook)
                .runCore(injector -> {
                    Service1 service1 = injector.getInstance(Service1.class);

                    Assertions.assertInstanceOf(Service1Stub.class, service1);
                    Assertions.assertEquals("moon", service1.get());

                    Service1Stub stub = (Service1Stub) service1;
                    hook.before();
                    Assertions.assertTrue(stub.beforeCalled);
                    hook.after();
                    Assertions.assertTrue(stub.afterCalled);

                    return null;
                });
    }


    public static class Service1 {

        public String get() {
            return "sun";
        }
    }

    public static class Service1Stub extends Service1 implements StubLifecycle {

        public static boolean created;

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
}
