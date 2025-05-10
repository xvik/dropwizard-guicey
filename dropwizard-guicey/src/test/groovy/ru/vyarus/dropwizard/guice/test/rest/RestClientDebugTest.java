package ru.vyarus.dropwizard.guice.test.rest;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.vyarus.dropwizard.guice.test.TestSupport;
import ru.vyarus.dropwizard.guice.test.rest.support.RestStubApp;

/**
 * @author Vyacheslav Rusakov
 * @since 09.05.2025
 */
public class RestClientDebugTest {

    @Test
    void testClientDebug() throws Exception {
        final RestStubsHook rest = RestStubsHook.builder()
                .build();
        TestSupport.build(RestStubApp.class)
                .hooks(rest)
                .runCore(injector -> {

                    String out = TestSupport.captureOutput(() ->
                            rest.getRestClient().get("/1/foo", String.class));

                    Assertions.assertThat(out.replaceAll("\r", "")
                                    .replaceAll("on thread ([^\n]+)", "on thread ddd"))
                            .contains("[Client action]---------------------------------------------{\n" +
                            "1 * Sending client request on thread ddd\n" +
                            "1 > GET http://localhost:0/1/foo\n" +
                            "\n" +
                            "}----------------------------------------------------------\n");

                    return null;
                });

    }

    @Test
    void testClientDebugDisabled() throws Exception {
        final RestStubsHook rest = RestStubsHook.builder()
                .logRequests(false)
                .build();
        TestSupport.build(RestStubApp.class)
                .hooks(rest)
                .runCore(injector -> {

                    String out = TestSupport.captureOutput(() ->
                            rest.getRestClient().get("/1/foo", String.class));

                    Assertions.assertThat(out).doesNotContain("[Client action]---------------------------------------------{");

                    return null;
                });

    }
}
