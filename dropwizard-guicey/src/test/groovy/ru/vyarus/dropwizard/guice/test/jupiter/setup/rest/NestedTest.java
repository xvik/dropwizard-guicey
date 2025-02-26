package ru.vyarus.dropwizard.guice.test.jupiter.setup.rest;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.rest.RestClient;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.rest.StubRest;
import ru.vyarus.dropwizard.guice.test.jupiter.setup.rest.support.RestStubApp;

/**
 * @author Vyacheslav Rusakov
 * @since 25.02.2025
 */
@TestGuiceyApp(RestStubApp.class)
public class NestedTest {

    @StubRest
    RestClient rest;

    @Test
    void test() {
        Assertions.assertNotNull(rest);
    }

    @Nested
    public class Nest {
        @Test
        void test() {
            Assertions.assertNotNull(rest);
        }
    }
}
