package ru.vyarus.dropwizard.guice.test.jupiter.setup.rest;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import ru.vyarus.dropwizard.guice.AbstractPlatformTest;
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp;
import ru.vyarus.dropwizard.guice.test.rest.RestClient;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.rest.StubRest;
import ru.vyarus.dropwizard.guice.test.rest.support.RestStubApp;

/**
 * @author Vyacheslav Rusakov
 * @since 22.02.2025
 */
public class RestClientDebugTest extends AbstractPlatformTest {

    @Test
    void testClientDebug() {
        String out = run(Test1.class);
        
        Assertions.assertThat(out).contains("[Client action]---------------------------------------------{\n" +
                "1 * Sending client request on thread ddd\n" +
                "1 > GET http://localhost:0/1/foo\n" +
                "\n" +
                "}----------------------------------------------------------\n" +
                "\n" +
                "\n" +
                "[Client action]---------------------------------------------{\n" +
                "1 * Client response received on thread ddd\n" +
                "1 < 200\n" +
                "1 < Content-Length: 3\n" +
                "1 < Content-Type: application/json\n" +
                "foo\n" +
                "\n" +
                "}----------------------------------------------------------");
    }

    @Test
    void testClientDebugDisabled() {
        String out = run(Test2.class);

        Assertions.assertThat(out).doesNotContain("[Client action]---------------------------------------------{");
    }

    @Override
    protected String clean(String out) {
        return out.replaceAll("on thread ([^\n]+)", "on thread ddd");
    }

    @TestGuiceyApp(RestStubApp.class)
    @Disabled
    public static class Test1 {

        @StubRest
        RestClient rest;

        @Test
        void test() {
            String res = rest.get("/1/foo", String.class);
            org.junit.jupiter.api.Assertions.assertEquals("foo", res);
        }
    }

    @TestGuiceyApp(RestStubApp.class)
    @Disabled
    public static class Test2 {

        @StubRest(logRequests = false)
        RestClient rest;

        @Test
        void test() {
            String res = rest.get("/1/foo", String.class);
            org.junit.jupiter.api.Assertions.assertEquals("foo", res);
        }
    }
}
