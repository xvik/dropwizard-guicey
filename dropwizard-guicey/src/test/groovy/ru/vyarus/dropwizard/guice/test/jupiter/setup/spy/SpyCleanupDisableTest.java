package ru.vyarus.dropwizard.guice.test.jupiter.setup.spy;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import ru.vyarus.dropwizard.guice.support.DefaultTestApp;
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.spy.SpyBean;

/**
 * @author Vyacheslav Rusakov
 * @since 17.02.2025
 */
@TestGuiceyApp(DefaultTestApp.class)
public class SpyCleanupDisableTest {

    @SpyBean(autoReset = false)
    static Service service;

    @Test
    void testMethod() {
        Mockito.when(service.foo()).thenReturn("bar");
    }

    @AfterAll
    static void afterAll() {
        // spy auto cleaned after each test - without cleanup, here will be overridden method
        Assertions.assertThat(service.foo()).isEqualTo("bar");
    }

    public static class Service {
        public String foo() {
            return "foo";
        }
    }
}
