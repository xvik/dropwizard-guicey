package ru.vyarus.dropwizard.guice.test.jupiter.setup.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.mockito.Mockito;
import ru.vyarus.dropwizard.guice.support.DefaultTestApp;
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.mock.MockBean;

/**
 * @author Vyacheslav Rusakov
 * @since 19.02.2025
 */
@TestGuiceyApp(DefaultTestApp.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MocksResetTest {

    @MockBean
    Service mock;

    @BeforeEach
    void setUp() {
        Mockito.when(mock.foo()).thenReturn("bar");
    }

    @Test
    void test1() {
        mock.foo();
        Mockito.verify(mock).foo();
    }

    @Test
    void test2() {
        mock.foo();
        // no second exec (put autoReset = false to make sure)
        Mockito.verify(mock).foo();
    }

    public static class Service {
        public String foo() {
            return "foo";
        }
    }
}
