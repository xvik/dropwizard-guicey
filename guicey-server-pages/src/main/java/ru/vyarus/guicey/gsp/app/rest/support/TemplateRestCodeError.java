package ru.vyarus.guicey.gsp.app.rest.support;

import ru.vyarus.guicey.gsp.app.util.TracelessException;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;

/**
 * Exception indicates error processing template handling rest call, when resource directly return non 200 code
 * (e.g. {@code Response.status(403).build()}) instead of exception.
 * <p>
 * Custom exception type used only to simplify direct no OK status error detection (differentiate from exceptions,
 * because in this case it is impossible to see exception origin in stack trace).
 *
 * @author Vyacheslav Rusakov
 * @see TemplateErrorResponseFilter
 * @since 29.01.2019
 */
public class TemplateRestCodeError extends WebApplicationException implements TracelessException {

    /**
     * Create a rest error.
     *
     * @param requestContext request context
     * @param status         response status
     */
    public TemplateRestCodeError(final ContainerRequestContext requestContext, final int status) {
        super("Error processing template rest call " + requestContext.getUriInfo().getPath() + ": " + status, status);
    }

    @Override
    public int getStatus() {
        return getResponse().getStatus();
    }
}
