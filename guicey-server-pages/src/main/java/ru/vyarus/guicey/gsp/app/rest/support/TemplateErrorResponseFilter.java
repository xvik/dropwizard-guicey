package ru.vyarus.guicey.gsp.app.rest.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vyarus.guicey.gsp.app.filter.redirect.ErrorRedirect;
import ru.vyarus.guicey.gsp.app.filter.redirect.TemplateRedirect;
import ru.vyarus.guicey.gsp.views.template.Template;
import ru.vyarus.guicey.gsp.views.template.TemplateContext;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

/**
 * Detect direct status response from rest (e.g. {@code Response.status(404).build()}) and tries to
 * redirect it into custom error page. Note that in this case dropwizard will log 404 rest response, but
 * actual response will return error page instead (so logs will be a bit misleading).
 *
 * @author Vyacheslav Rusakov
 * @since 15.01.2019
 */
@Provider
@Template
public class TemplateErrorResponseFilter implements ContainerResponseFilter {
    private final Logger logger = LoggerFactory.getLogger(TemplateErrorResponseFilter.class);

    @Override
    public void filter(final ContainerRequestContext requestContext,
                       final ContainerResponseContext responseContext) throws IOException {
        final int status = responseContext.getStatus();
        if (status >= ErrorRedirect.CODE_400) {
            // redirect direct status return from rest into error page (e.g. when
            // Response.status(400).build() used as response)
            final TemplateContext context = TemplateRedirect.templateContext();
            // response could be committed if it was already handled, for example exception handler detect exception
            // and error page was served instead, still this listener will be called and should do nothing
            if (context != null && !context.getResponse().isCommitted()) {
                logger.debug("Rest response code {} detected for template path '{}'",
                        status, requestContext.getUriInfo().getPath());
                context.redirectError(new TemplateRestCodeError(requestContext, status));
            }
        }
    }
}
