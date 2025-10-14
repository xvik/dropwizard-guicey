package ru.vyarus.dropwizard.guice.test.client.builder.util;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.ClientErrorException;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.NotAcceptableException;
import jakarta.ws.rs.NotAllowedException;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.NotSupportedException;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.RedirectionException;
import jakarta.ws.rs.ServerErrorException;
import jakarta.ws.rs.ServiceUnavailableException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.client.ResponseProcessingException;
import jakarta.ws.rs.core.Response;
import org.glassfish.jersey.client.internal.LocalizationMessages;

/**
 * Jersey provides additional exception handling for shortcut executions with response type (like
 * {@code Invocation.Builder#method("GET", SomeClass.class)}). But it is impossible to re-use this logic
 * when {@link jakarta.ws.rs.core.Response} is obtained.
 * <p>
 * This class contain a copy of exceptions logic from
 * {@link org.glassfish.jersey.client.JerseyInvocation#translate(
 * org.glassfish.jersey.client.ClientResponse, org.glassfish.jersey.process.internal.RequestScope, Class)}.
 *
 * @author Vyacheslav Rusakov
 * @since 19.09.2025
 */
@SuppressWarnings({"checkstyle:ClassDataAbstractionCoupling", "checkstyle:CyclomaticComplexity"})
public final class JerseyExceptionHandling {

    private JerseyExceptionHandling() {
    }

    /**
     * Throw custom exception for not successful response (not 2xx). The exception would be the same as with
     * direct value call like {@code Invocation.Builder#method("GET", SomeClass.class)}.
     *
     * @param response response to check and theow exceptions.
     * @throws ProcessingException for not successful request
     */
    public static void throwIfNotSuccess(final Response response) throws ProcessingException {
        if (response.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
            final ProcessingException ex = convertToException(response);
            if (ex.getCause() instanceof WebApplicationException) {
                throw (WebApplicationException) ex.getCause();
            } else {
                throw ex;
            }
        }
    }

    /**
     * Convert response to the exception. Assumed failed response object.
     *
     * @param response response
     * @return specialized exception (according to response status)
     */
    public static ProcessingException convertToException(final Response response) {
        final int statusCode = response.getStatus();

        try {
            // Buffer and close entity input stream (if any) to prevent
            // leaking connections (see JERSEY-2157).
            response.bufferEntity();

            final WebApplicationException webAppException;
            final Response.Status status = Response.Status.fromStatusCode(statusCode);

            if (status == null) {
                final Response.Status.Family statusFamily = response.getStatusInfo().getFamily();
                webAppException = createExceptionForFamily(response, statusFamily);
            } else {
                webAppException = switch (status) {
                    case BAD_REQUEST -> new BadRequestException(response);
                    case UNAUTHORIZED -> new NotAuthorizedException(response);
                    case FORBIDDEN -> new ForbiddenException(response);
                    case NOT_FOUND -> new NotFoundException(response);
                    case METHOD_NOT_ALLOWED -> new NotAllowedException(response);
                    case NOT_ACCEPTABLE -> new NotAcceptableException(response);
                    case UNSUPPORTED_MEDIA_TYPE -> new NotSupportedException(response);
                    case INTERNAL_SERVER_ERROR -> new InternalServerErrorException(response);
                    case SERVICE_UNAVAILABLE -> new ServiceUnavailableException(response);
                    default -> {
                        final Response.Status.Family statusFamily = response.getStatusInfo().getFamily();
                        yield createExceptionForFamily(response, statusFamily);
                    }
                };
            }

            return new ResponseProcessingException(response, webAppException);
        } catch (final Throwable t) {
            return new ResponseProcessingException(response,
                    LocalizationMessages.RESPONSE_TO_EXCEPTION_CONVERSION_FAILED(), t);
        }
    }

    private static WebApplicationException createExceptionForFamily(final Response response,
                                                                    final Response.Status.Family statusFamily) {
        return switch (statusFamily) {
            case REDIRECTION -> new RedirectionException(response);
            case CLIENT_ERROR -> new ClientErrorException(response);
            case SERVER_ERROR -> new ServerErrorException(response);
            default -> new WebApplicationException(response);
        };
    }
}
