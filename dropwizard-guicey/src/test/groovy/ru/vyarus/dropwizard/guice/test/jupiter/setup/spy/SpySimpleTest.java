package ru.vyarus.dropwizard.guice.test.jupiter.setup.spy;

import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.internal.util.MockUtil;
import ru.vyarus.dropwizard.guice.support.DefaultTestApp;
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.spy.SpyBean;

/**
 * @author Vyacheslav Rusakov
 * @since 10.02.2025
 */
@TestGuiceyApp(value = DefaultTestApp.class, debug = true)
public class SpySimpleTest {

    @SpyBean
    Service1 spy1;
    @SpyBean
    static Service2 spy2;


    @Inject
    OuterService outerService;

    @BeforeAll
    static void beforeAll() {
        Assertions.assertNotNull(spy2);
    }

    @BeforeEach
    void setUp() {
        Assertions.assertNotNull(spy1);
        Assertions.assertNotNull(spy2);
    }

    @Test
    void testSpyInjection() {
        Assertions.assertNotNull(spy1);
        Assertions.assertNotNull(spy2);
        Assertions.assertTrue(MockUtil.isSpy(spy1));
        Assertions.assertTrue(MockUtil.isSpy(spy2));
        Assertions.assertEquals("Hello 11 Hello 11", outerService.doSomething(11));
        Mockito.verify(spy1, Mockito.times(1)).get(11);
        Mockito.verify(spy2, Mockito.times(1)).get(11);
    }

    public static class Service1 {

        public String get(int id) {
            return "Hello " + id;
        }
    }

    public static class Service2 {

        public String get(int id) {
            return "Hello " + id;
        }
    }

    public static class OuterService {

        @Inject
        Service1 service1;
        @Inject
        Service2 service2;

        public String doSomething(int id) {
            return service1.get(id) + " " + service2.get(id);
        }
    }
}
