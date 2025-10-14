package ru.vyarus.dropwizard.guice.test.client;

import org.junit.jupiter.api.Test;
import ru.vyarus.dropwizard.guice.test.ClientSupport;
import ru.vyarus.dropwizard.guice.test.client.builder.util.VoidBodyReader;
import ru.vyarus.dropwizard.guice.test.client.support.ClientApp;
import ru.vyarus.dropwizard.guice.test.client.support.Resource;
import ru.vyarus.dropwizard.guice.test.jupiter.TestDropwizardApp;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Vyacheslav Rusakov
 * @since 13.10.2025
 */
@TestDropwizardApp(ClientApp.class)
public class BaseClientTest {

    @Test
    void testBaseMethods(ClientSupport client) {
        final TestClient<?> app = client.appClient();
        assertThat(app.toString()).isEqualTo("Client for: http://localhost:8080/");
        assertThat(app.getBaseUri().toString()).isEqualTo("http://localhost:8080/");

        final List<String> called = new ArrayList<>();

        app.defaultPathConfiguration(webTarget -> {
            called.add("called");
            return webTarget;
        });

        // defaults not applied
        app.target("/root/get").request().get();
        assertThat(called).isEmpty();

        // defaults applied
        app.request("/root/get").get();
        assertThat(called).containsExactly("called");

        called.clear();
        app.subClient(builder -> builder.path("/root/")).get("/get");
        assertThat(called).containsExactly("called");

        called.clear();
        app.subClient(builder -> builder.path("/root/"), Resource.class).get("/get");
        assertThat(called).containsExactly("called");

        called.clear();
        app.subClient("/root/").asRestClient(Resource.class).get("/get");
        assertThat(called).containsExactly("called");
    }

    @Test
    void testArrayQueryParams(ClientSupport client) {
        final TestClient<?> app = client.appClient();
        app.defaultQueryParam("q", new Object[]{"1", "2"});

        app.buildGet("/root/get")
                .assertRequest(tracker ->
                        assertThat(tracker.getUrl()).endsWith("?q=1&q=2"))
                .asVoid();

        app.reset().defaultMatrixParam("m", new Object[]{"1", "2"});

        app.buildGet("/root/get")
                .assertRequest(tracker ->
                        assertThat(tracker.getUrl()).endsWith(";m=1;m=2"))
                .asVoid();
    }

    @Test
    void testNullExtension(ClientSupport client) {
        final TestClient<?> app = client.appClient();
        app.defaultRegister(VoidBodyReader.class, null);

        app.buildGet("/root/get").asVoid();
    }
}
