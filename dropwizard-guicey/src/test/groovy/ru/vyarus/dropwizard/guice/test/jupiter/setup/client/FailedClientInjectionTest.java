package ru.vyarus.dropwizard.guice.test.jupiter.setup.client;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import ru.vyarus.dropwizard.guice.AbstractPlatformTest;
import ru.vyarus.dropwizard.guice.support.DefaultTestApp;
import ru.vyarus.dropwizard.guice.test.client.TestClient;
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.client.WebClient;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Vyacheslav Rusakov
 * @since 17.10.2025
 */
public class FailedClientInjectionTest extends AbstractPlatformTest {

    @Test
    void testClientTypeValidation() {
        Throwable ex = runFailed(Test1.class);
        assertThat(ex.getMessage()).isEqualTo("ClientSupport type must be used for the default @WebClient field: FailedClientInjectionTest$Test1.client");
    }

    @Disabled
    @TestGuiceyApp(DefaultTestApp.class)
    public static class Test1 {

        @WebClient
        TestClient<?> client;

        @Test
        void test() {

        }
    }

    @Override
    protected String clean(String out) {
        return out;
    }
}
