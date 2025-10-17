package ru.vyarus.dropwizard.guice.test.jupiter.setup.client.rest;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import ru.vyarus.dropwizard.guice.AbstractPlatformTest;
import ru.vyarus.dropwizard.guice.test.client.ResourceClient;
import ru.vyarus.dropwizard.guice.test.jupiter.TestDropwizardApp;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.client.rest.WebResourceClient;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Vyacheslav Rusakov
 * @since 17.10.2025
 */
public class RestClientResetTest extends AbstractPlatformTest {

    @Test
    void testDefaultsReset() {
        runSuccess(Test1.class, Test2.class);
    }

    @TestDropwizardApp(RestApp.class)
    @Disabled
    public static class Test1 {

        @WebResourceClient
        static ResourceClient<RestApp.Resource> client;

        @Test
        void test1() {
            client.defaultLanguage(Locale.ENGLISH);
        }

        @AfterAll
        static void afterAll() {
            assertThat(client.hasDefaults()).isFalse();
        }
    }

    @TestDropwizardApp(RestApp.class)
    @Disabled
    public static class Test2 {

        @WebResourceClient(autoReset = false)
        static ResourceClient<RestApp.Resource> client;

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
