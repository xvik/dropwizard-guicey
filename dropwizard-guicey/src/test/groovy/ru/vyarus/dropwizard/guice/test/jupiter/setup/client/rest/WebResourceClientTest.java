package ru.vyarus.dropwizard.guice.test.jupiter.setup.client.rest;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.vyarus.dropwizard.guice.test.client.ResourceClient;
import ru.vyarus.dropwizard.guice.test.jupiter.TestDropwizardApp;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.client.rest.WebResourceClient;

/**
 * @author Vyacheslav Rusakov
 * @since 17.10.2025
 */
@TestDropwizardApp(RestApp.class)
public class WebResourceClientTest {

    @WebResourceClient
    ResourceClient<RestApp.Resource> rest;

    @Test
    void testRestClient() {
        Assertions.assertThat(rest.method(RestApp.Resource::get).as(String.class)).isEqualTo("ok");
    }
}
