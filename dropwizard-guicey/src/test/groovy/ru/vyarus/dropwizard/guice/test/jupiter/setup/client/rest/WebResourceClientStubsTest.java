package ru.vyarus.dropwizard.guice.test.jupiter.setup.client.rest;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.vyarus.dropwizard.guice.test.client.ResourceClient;
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.client.rest.WebResourceClient;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.rest.StubRest;
import ru.vyarus.dropwizard.guice.test.rest.RestClient;

/**
 * @author Vyacheslav Rusakov
 * @since 17.10.2025
 */
@TestGuiceyApp(RestApp.class)
public class WebResourceClientStubsTest {

    @StubRest
    RestClient client;

    @WebResourceClient
    ResourceClient<RestApp.Resource> rest;

    @Test
    void testRestClient() {
        Assertions.assertThat(rest.method(RestApp.Resource::get).as(String.class)).isEqualTo("ok");
    }
}
