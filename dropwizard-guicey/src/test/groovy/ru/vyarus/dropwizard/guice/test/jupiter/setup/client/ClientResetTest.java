package ru.vyarus.dropwizard.guice.test.jupiter.setup.client;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import ru.vyarus.dropwizard.guice.AbstractPlatformTest;
import ru.vyarus.dropwizard.guice.support.DefaultTestApp;
import ru.vyarus.dropwizard.guice.test.ClientSupport;
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.client.WebClient;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Vyacheslav Rusakov
 * @since 17.10.2025
 */
public class ClientResetTest extends AbstractPlatformTest {

    @Test
    void testDefaultsReset() {
        runSuccess(Test1.class, Test2.class);
    }

    @TestGuiceyApp(DefaultTestApp.class)
    @Disabled
    public static class Test1 {

        @WebClient
        static ClientSupport client;

        @Test
        void test1() {
            client.defaultLanguage(Locale.ENGLISH);
        }

        @AfterAll
        static void afterAll() {
            assertThat(client.hasDefaults()).isFalse();
        }
    }

    @TestGuiceyApp(DefaultTestApp.class)
    @Disabled
    public static class Test2 {

        @WebClient(autoReset = false)
        static ClientSupport client;

        @Test
        void test1() {
            client.defaultLanguage(Locale.ENGLISH);
        }

        @AfterAll
        static void afterAll() {
            assertThat(client.hasDefaults()).isTrue();
        }
    }

    @Override
    protected String clean(String out) {
        return out;
    }
}
