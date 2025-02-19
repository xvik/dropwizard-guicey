package ru.vyarus.dropwizard.guice.test.jupiter.setup.mock;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.mockito.Mockito;
import ru.vyarus.dropwizard.guice.support.DefaultTestApp;
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.mock.MockBean;

/**
 * @author Vyacheslav Rusakov
 * @since 17.02.2025
 */
@TestGuiceyApp(value = DefaultTestApp.class, debug = true)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MockSimpleTest {

    @Inject
    Service1 service1;

    @Inject
    Service2 service2;

    @MockBean
    Service1 mock1;

    @MockBean
    static Service2 mock2;

    @BeforeAll
    static void beforeAll() {
        Preconditions.checkNotNull(mock2);
    }

    @BeforeEach
    void setUp() {
        Preconditions.checkNotNull(mock1);
        Preconditions.checkNotNull(mock2);

        Mockito.when(mock1.foo()).thenReturn("bar1");
        Mockito.when(mock2.foo()).thenReturn("bar2");
    }

    @Test
    @Order(1)
    void testStub() {
        Assertions.assertEquals(service1, mock1);
        Assertions.assertEquals(service2, mock2);
        Assertions.assertEquals("bar1", service1.foo());
        Assertions.assertEquals("bar2", service2.foo());
    }

    @Test
    @Order(2)
    void testStub2() {
        Assertions.assertEquals(service1, mock1);
        Assertions.assertEquals(service2, mock2);
        Assertions.assertEquals("bar1", service1.foo());
    }

    public static class Service1 {
        public String foo() {
            return "foo1";
        }
    }

    public static class Service2 {
        public String foo() {
            return "foo2";
        }
    }
}
