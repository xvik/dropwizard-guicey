package ru.vyarus.dropwizard.guice.test.jupiter.setup.mock;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import ru.vyarus.dropwizard.guice.support.DefaultTestApp;
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.mock.MockBean;

/**
 * @author Vyacheslav Rusakov
 * @since 14.02.2025
 */
@TestGuiceyApp(DefaultTestApp.class)
public class MockCleanupDisableTest {

    @MockBean(autoReset = false)
    static Service mock;

    @Test
    void testMethod() {
        Mockito.when(mock.foo()).thenReturn("bar");
    }

    @AfterAll
    static void afterAll() {
        // mock auto cleaned after each test - without cleanup, here will be overridden method
        Assertions.assertThat(mock.foo()).isEqualTo("bar");
    }

    public static class Service {
        public String foo() {
            return "foo";
        }
    }
}
