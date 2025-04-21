package ru.vyarus.dropwizard.guice.test.jupiter.setup.rest;

import javax.ws.rs.core.MediaType;
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
 * @since 26.02.2025
 */
public class ClientDefaultsTest extends AbstractPlatformTest {

    @Test
    void testDefaults() {
        String out = run(Test1.class);
        Assertions.assertThat(out).contains("[Client action]---------------------------------------------{\n" +
                "1 * Sending client request on thread ddd\n" +
                "1 > GET http://localhost:0/1/foo?par1=val1&par2=val2\n" +
                "1 > Accept: application/json\n" +
                "1 > Boo: baz\n" +
                "1 > Foo: bar\n" +
                "\n" +
                "}----------------------------------------------------------");
    }

    @TestGuiceyApp(RestStubApp.class)
    @Disabled
    public static class Test1 {

        @StubRest(logRequests = true)
        RestClient rest;

        @Test
        void test() {
            rest.defaultAccept(MediaType.APPLICATION_JSON);
            rest.defaultHeader("Foo", "bar");
            rest.defaultHeader("Boo", "baz");
            rest.defaultQueryParam("par1", "val1");
            rest.defaultQueryParam("par2", "val2");
            rest.get("/1/foo", String.class);

            org.junit.jupiter.api.Assertions.assertTrue(rest.hasDefaultAccepts());
            org.junit.jupiter.api.Assertions.assertTrue(rest.hasDefaultHeaders());
            org.junit.jupiter.api.Assertions.assertTrue(rest.hasDefaultQueryParams());
        }
    }

    @Override
    protected String clean(String out) {
        return out.replaceAll("on thread ([^\n]+)", "on thread ddd");
    }
}
