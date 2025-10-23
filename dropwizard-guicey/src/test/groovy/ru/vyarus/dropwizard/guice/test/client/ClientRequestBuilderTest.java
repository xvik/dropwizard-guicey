package ru.vyarus.dropwizard.guice.test.client;

import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.core.GenericType;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.vyarus.dropwizard.guice.test.ClientSupport;
import ru.vyarus.dropwizard.guice.test.client.builder.TestClientResponse;
import ru.vyarus.dropwizard.guice.test.client.support.ClientApp;
import ru.vyarus.dropwizard.guice.test.client.support.SuccFailRedirectResource;
import ru.vyarus.dropwizard.guice.test.jupiter.TestDropwizardApp;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Vyacheslav Rusakov
 * @since 08.10.2025
 */
@TestDropwizardApp(value = ClientApp.class, apacheClient = true)
public class ClientRequestBuilderTest {

    @Test
    void testInvocations(ClientSupport client) {
        final ResourceClient<SuccFailRedirectResource> rest = client.restClient(SuccFailRedirectResource.class);

        rest.method(SuccFailRedirectResource::get).asVoid();
        assertThat(rest.method(SuccFailRedirectResource::get).as(String.class)).isEqualTo("ok");
        assertThat(rest.method(SuccFailRedirectResource::get).as(new GenericType<String>() {})).isEqualTo("ok");
        
        TestClientResponse response = rest.method(SuccFailRedirectResource::get).invoke();
        assertThat(response.as(String.class)).isEqualTo("ok");

        response = rest.method("post", "test").invoke();
        assertThat(response.as(String.class)).isEqualTo("test");
    }

    @Test
    void testStatusCheck(ClientSupport client) {
        final ResourceClient<SuccFailRedirectResource> rest = client.restClient(SuccFailRedirectResource.class);
        
        // WHEN success - expect success
        TestClientResponse response = rest.method(SuccFailRedirectResource::get).expectSuccess();
        assertThat(response.asResponse().getStatus()).isEqualTo(200);

        // WHEN success - expect failure
        Assertions.assertThatThrownBy(() -> rest.method(SuccFailRedirectResource::get).expectFailure())
                        .isInstanceOf(AssertionError.class)
                                .hasMessage("Failed response expected, but found 'SUCCESSFUL' ==> expected: not equal but was: <SUCCESSFUL>");

        // WHEN success 200 - expect 201
        Assertions.assertThatThrownBy(() -> rest.method(SuccFailRedirectResource::get).expectSuccess(201))
                .isInstanceOf(AssertionError.class)
                .hasMessage("Unexpected response status 200 when expected 201 ==> expected: <true> but was: <false>");


        // WHEN failure - expect failure
        response = rest.method(SuccFailRedirectResource::error).expectFailure();
        assertThat(response.asResponse().getStatus()).isEqualTo(500);

        // WHEN failure = expect success
        Assertions.assertThatThrownBy(() -> rest.method(SuccFailRedirectResource::error).expectSuccess())
                .isInstanceOf(InternalServerErrorException.class)
                .hasMessage("HTTP 500 Server Error");

        // WHEN failure 500 - expect 401
        Assertions.assertThatThrownBy(() -> rest.method(SuccFailRedirectResource::error).expectFailure(401))
                .isInstanceOf(AssertionError.class)
                .hasMessage("Unexpected response status 500 when expected 401 ==> expected: <true> but was: <false>");

        // WHEN redirect - expect redirect
        response = rest.method(SuccFailRedirectResource::redirect).expectRedirect();
        assertThat(response.asResponse().getStatus()).isEqualTo(303);
        assertThat(response.asResponse().getHeaderString("Location")).isNotBlank();

        // WHEN no redirect - expect redirect
        Assertions.assertThatThrownBy(() -> rest.method(SuccFailRedirectResource::get).expectRedirect())
                .isInstanceOf(AssertionError.class)
                .hasMessage("Expected 'REDIRECTION' response status, but found 'SUCCESSFUL' ==> expected: <REDIRECTION> but was: <SUCCESSFUL>");

        // WHEN error - expect redirect
        Assertions.assertThatThrownBy(() -> rest.method(SuccFailRedirectResource::error).expectRedirect())
                .isInstanceOf(AssertionError.class)
                .hasMessage("Expected 'REDIRECTION' response status, but found 'SERVER_ERROR' ==> expected: <REDIRECTION> but was: <SERVER_ERROR>");

        // WHEN redirect 303 - expect redirect 301
        Assertions.assertThatThrownBy(() -> rest.method(SuccFailRedirectResource::redirect).expectRedirect(301))
                .isInstanceOf(AssertionError.class)
                .hasMessage("Unexpected response status 303 when expected 301 ==> expected: <true> but was: <false>");
    }
}
