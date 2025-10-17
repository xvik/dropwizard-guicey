package ru.vyarus.dropwizard.guice.test.jupiter.setup.client;

import org.junit.jupiter.api.Test;
import ru.vyarus.dropwizard.guice.support.DefaultTestApp;
import ru.vyarus.dropwizard.guice.test.ClientSupport;
import ru.vyarus.dropwizard.guice.test.client.TestClient;
import ru.vyarus.dropwizard.guice.test.jupiter.TestDropwizardApp;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.client.WebClient;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.client.WebClientType;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Vyacheslav Rusakov
 * @since 17.10.2025
 */
@TestDropwizardApp(value = DefaultTestApp.class, configOverride = {
        "server.applicationContextPath: /app",
        "server.adminContextPath: /admin",
}, restMapping = "api")
public class ClientInjectionTest {

    @WebClient
    ClientSupport client;

    @WebClient(WebClientType.App)
    TestClient<?> app;

    @WebClient(WebClientType.Admin)
    TestClient<?> admin;

    @WebClient(WebClientType.Rest)
    TestClient<?> rest;

    @Test
    void testClientInjection(ClientSupport support) {
        assertThat(app).isNotNull();
        assertThat(admin).isNotNull();
        assertThat(rest).isNotNull();

        assertThat(client).isNotNull().isSameAs(support);
        assertThat(app.getBaseUri()).isEqualTo(support.appClient().getBaseUri());
        assertThat(admin.getBaseUri()).isEqualTo(support.adminClient().getBaseUri());
        assertThat(rest.getBaseUri()).isEqualTo(support.restClient().getBaseUri());

    }
}
