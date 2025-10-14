package ru.vyarus.dropwizard.guice.test.jupiter.setup.rest;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import ru.vyarus.dropwizard.guice.AbstractPlatformTest;
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp;
import ru.vyarus.dropwizard.guice.test.rest.RestClient;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.rest.StubRest;
import ru.vyarus.dropwizard.guice.test.rest.support.RestStubApp;

/**
 * @author Vyacheslav Rusakov
 * @since 26.02.2025
 */
public class RestClientStateResetTest extends AbstractPlatformTest {

    @Test
    void testAutoRest() {
        runSuccess(Test1.class, Test2.class);
    }

    @TestGuiceyApp(RestStubApp.class)
    @Disabled
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    public static class Test1 {

        @StubRest
        RestClient rest;

        @Test
        @Order(1)
        void test() {
            rest.defaultHeader("Foo", "bar");
            rest.defaultAccept("dsfdsfd");
            rest.defaultQueryParam("foo", "bar");
        }

        @Test
        @Order(2)
        void test2() {
            Assertions.assertFalse(rest.hasDefaultAccepts());
            Assertions.assertFalse(rest.hasDefaultHeaders());
            Assertions.assertFalse(rest.hasDefaultQueryParams());
        }
    }

    @TestGuiceyApp(RestStubApp.class)
    @Disabled
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    public static class Test2 {

        @StubRest(autoReset = false)
        RestClient rest;

        @Test
        @Order(1)
        void test() {
            rest.defaultHeader("Foo", "bar");
            rest.defaultAccept("dsfdsfd");
            rest.defaultQueryParam("foo", "bar");
        }

        @Test
        @Order(2)
        void test2() {
            Assertions.assertTrue(rest.hasDefaultAccepts());
            Assertions.assertTrue(rest.hasDefaultHeaders());
            Assertions.assertTrue(rest.hasDefaultQueryParams());
        }
    }

    @Override
    protected String clean(String out) {
        return out;
    }
}
