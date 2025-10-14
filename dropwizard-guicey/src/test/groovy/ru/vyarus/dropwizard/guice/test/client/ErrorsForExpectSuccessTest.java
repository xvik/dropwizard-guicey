package ru.vyarus.dropwizard.guice.test.client;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.ClientErrorException;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.NotAcceptableException;
import jakarta.ws.rs.NotAllowedException;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.NotSupportedException;
import jakarta.ws.rs.RedirectionException;
import jakarta.ws.rs.ServerErrorException;
import jakarta.ws.rs.ServiceUnavailableException;
import jakarta.ws.rs.WebApplicationException;
import org.junit.jupiter.api.Test;
import ru.vyarus.dropwizard.guice.test.ClientSupport;
import ru.vyarus.dropwizard.guice.test.client.support.ClientApp;
import ru.vyarus.dropwizard.guice.test.client.support.ErrorsResource;
import ru.vyarus.dropwizard.guice.test.jupiter.TestDropwizardApp;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author Vyacheslav Rusakov
 * @since 12.10.2025
 */
@TestDropwizardApp(ClientApp.class)
public class ErrorsForExpectSuccessTest {
    // expectSuccess re-implements jersey exceptions throwing logic - check it's unified with jersey behaviour

    @Test
    public void testErrors(ClientSupport client) {
        final ResourceClient<ErrorsResource> rest = client.restClient(ErrorsResource.class);

        assertThatThrownBy(() -> rest.method(ErrorsResource::bad).as(String.class))
                .isInstanceOf(BadRequestException.class);
        assertThatThrownBy(() -> rest.method(ErrorsResource::bad).expectSuccess())
                .isInstanceOf(BadRequestException.class);

        assertThatThrownBy(() -> rest.method(ErrorsResource::unauth).as(String.class))
                .isInstanceOf(NotAuthorizedException.class);
        assertThatThrownBy(() -> rest.method(ErrorsResource::unauth).expectSuccess())
                .isInstanceOf(NotAuthorizedException.class);

        assertThatThrownBy(() -> rest.method(ErrorsResource::forbid).as(String.class))
                .isInstanceOf(ForbiddenException.class);
        assertThatThrownBy(() -> rest.method(ErrorsResource::forbid).expectSuccess())
                .isInstanceOf(ForbiddenException.class);

        assertThatThrownBy(() -> rest.buildGet("/notexistingmethod").as(String.class))
                .isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> rest.buildGet("/notexistingmethod").expectSuccess())
                .isInstanceOf(NotFoundException.class);

        assertThatThrownBy(() -> rest.buildDelete("/bad").as(String.class))
                .isInstanceOf(NotAllowedException.class);
        assertThatThrownBy(() -> rest.buildDelete("/bad").expectSuccess())
                .isInstanceOf(NotAllowedException.class);

        assertThatThrownBy(() -> rest.method(ErrorsResource::notacc).as(String.class))
                .isInstanceOf(NotAcceptableException.class);
        assertThatThrownBy(() -> rest.method(ErrorsResource::notacc).expectSuccess())
                .isInstanceOf(NotAcceptableException.class);

        assertThatThrownBy(() -> rest.method(ErrorsResource::unsupported).as(String.class))
                .isInstanceOf(NotSupportedException.class);
        assertThatThrownBy(() -> rest.method(ErrorsResource::unsupported).expectSuccess())
                .isInstanceOf(NotSupportedException.class);

        assertThatThrownBy(() -> rest.method(ErrorsResource::error).as(String.class))
                .isInstanceOf(InternalServerErrorException.class);
        assertThatThrownBy(() -> rest.method(ErrorsResource::error).expectSuccess())
                .isInstanceOf(InternalServerErrorException.class);

        assertThatThrownBy(() -> rest.method(ErrorsResource::unavailable).as(String.class))
                .isInstanceOf(ServiceUnavailableException.class);
        assertThatThrownBy(() -> rest.method(ErrorsResource::unavailable).expectSuccess())
                .isInstanceOf(ServiceUnavailableException.class);

        assertThatThrownBy(() -> rest.method(ErrorsResource::customClient).as(String.class))
                .isInstanceOf(ClientErrorException.class);
        assertThatThrownBy(() -> rest.method(ErrorsResource::customClient).expectSuccess())
                .isInstanceOf(ClientErrorException.class);

        assertThatThrownBy(() -> rest.method(ErrorsResource::customServer).as(String.class))
                .isInstanceOf(ServerErrorException.class);
        assertThatThrownBy(() -> rest.method(ErrorsResource::customServer).expectSuccess())
                .isInstanceOf(ServerErrorException.class);

        assertThatThrownBy(() -> rest.method(ErrorsResource::customRedirect).as(String.class))
                .isInstanceOf(RedirectionException.class);
        assertThatThrownBy(() -> rest.method(ErrorsResource::customRedirect).expectSuccess())
                .isInstanceOf(RedirectionException.class);

        assertThatThrownBy(() -> rest.method(ErrorsResource::informal).as(String.class))
                .isInstanceOf(WebApplicationException.class);
        assertThatThrownBy(() -> rest.method(ErrorsResource::informal).expectSuccess())
                .isInstanceOf(WebApplicationException.class);
    }
}
