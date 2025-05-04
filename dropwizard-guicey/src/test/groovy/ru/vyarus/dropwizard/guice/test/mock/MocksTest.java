package ru.vyarus.dropwizard.guice.test.mock;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import ru.vyarus.dropwizard.guice.support.DefaultTestApp;
import ru.vyarus.dropwizard.guice.test.TestSupport;

/**
 * @author Vyacheslav Rusakov
 * @since 04.05.2025
 */
public class MocksTest {

    @Test
    void testMocks() throws Exception {
        MocksHook hook = new MocksHook();
        final Service1 mock = hook.mock(Service1.class);
        Mockito.when(mock.foo()).thenReturn("bar1");

        TestSupport.build(DefaultTestApp.class)
                .hooks(hook)
                .runCore(injector -> {
                    Service1 service1 = injector.getInstance(Service1.class);

                    Assertions.assertEquals(service1, mock);
                    Assertions.assertEquals("bar1", service1.foo());

                    Assertions.assertEquals(mock, hook.getMock(Service1.class));

                    hook.resetMocks();
                    Assertions.assertNull(service1.foo());
                    return null;
                });
    }

    @Test
    void testManualMock() throws Exception {
        MocksHook hook = new MocksHook();
        final Service1 mock = Mockito.mock(Service1.class);
        final Service1 mock2 = hook.mock(Service1.class, mock);

        Assertions.assertEquals(mock, mock2);

        Mockito.when(mock.foo()).thenReturn("bar1");

        TestSupport.build(DefaultTestApp.class)
                .hooks(hook)
                .runCore(injector -> {
                    Service1 service1 = injector.getInstance(Service1.class);

                    Assertions.assertEquals(service1, mock);
                    Assertions.assertEquals("bar1", service1.foo());
                    return null;
                });
    }

    public static class Service1 {
        public String foo() {
            return "foo1";
        }
    }
}
