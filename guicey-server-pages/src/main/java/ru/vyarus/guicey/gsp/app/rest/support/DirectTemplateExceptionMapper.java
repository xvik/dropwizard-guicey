package ru.vyarus.guicey.gsp.app.rest.support;

import org.glassfish.jersey.server.monitoring.RequestEvent;
import org.glassfish.jersey.spi.ExtendedExceptionMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vyarus.dropwizard.guice.module.installer.feature.jersey.JerseyManaged;
import ru.vyarus.dropwizard.guice.module.installer.util.PathUtils;
import ru.vyarus.guicey.gsp.app.filter.redirect.ErrorRedirect;
import ru.vyarus.guicey.gsp.app.filter.redirect.TemplateRedirect;
import ru.vyarus.guicey.gsp.views.template.ErrorTemplateView;
import ru.vyarus.guicey.gsp.views.template.TemplateContext;
import ru.vyarus.guicey.gsp.views.template.TemplateNotFoundException;
import ru.vyarus.guicey.gsp.views.template.TemplateView;

import javax.inject.Inject;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Provider;

/**
 * {@code NotFoundException} thrown by jersey when matching rest path not found. Exception mapper detects it
 * and if it is template call tries to render template directly instead.
 * <p>
 * Handler is applied to all rest, but as its an extended mapper, it will not influence normal rest calls.
 * <p>
 * NOTE: application may declare different exception mapper for {@link NotFoundException} and so it may
 * override this mapper. It is highly unlikely, but still could happen.
 * <p>
 * Another drawback is template rendering errors will be unreachable for default dropwizard exception mapper
 * ({@link io.dropwizard.jersey.errors.IllegalStateExceptionMapper} which logs all rest exceptions. But
 * this is compensated by direct exception error logging.
 *
 * @author Vyacheslav Rusakov
 * @since 06.12.2019
 */
@Provider
// because for guice managed bean original UriInfo returned even after error redirect
@JerseyManaged
public class DirectTemplateExceptionMapper implements ExtendedExceptionMapper<NotFoundException> {
    private final Logger logger = LoggerFactory.getLogger(DirectTemplateExceptionMapper.class);

    @Inject
    private javax.inject.Provider<UriInfo> info;

    @Override
    public boolean isMappable(final NotFoundException exception) {
        // be sure that it's a template call (at least one view renderer recognized it)
        // and no resource paths were matched
        return isDirectTemplateRequest() && info.get().getMatchedResources().isEmpty();
    }

    @Override
    public Response toResponse(final NotFoundException exception) {
        final TemplateContext context = TemplateContext.getInstance();
        final String path = info.get().getPath().substring(context.getRestPrefix().length() - 1);
        final String fullPath = PathUtils.path(context.getRestSubContext(), path);
        logger.debug("Direct template rendering: '{}'", fullPath);
        Response res;
        try {
            res = Response
                    .ok(ErrorRedirect.hasContextError() ? new ErrorTemplateView(path) : new TemplateView(path))
                    // for the majority of cases it would be html, for custom resource templates (css, json, etc.)
                    // use custom view mappings where exact type could be declared
                    .type(MediaType.TEXT_HTML_TYPE)
                    .build();
        } catch (TemplateNotFoundException ex) {
            // template not found
            final String message = "Template '" + path + "' not found";
            TemplateContext.getInstance().redirectError(new NotFoundException(message, ex));
            res = Response.status(404, message).build();
        } catch (Throwable ex) {
            // have to handle exception here manually, because jersey will not allow to redirect after exception
            // mapper fail (if exception would be thrown outside from here)
            logger.error("Error rendering direct template ex", ex);
            // either error will be redirected to error page or it's a error page rendering failure
            TemplateContext.getInstance().redirectError(exception);
            res = Response.serverError().build();
        }
        return res;
    }

    /**
     * Exception event first will be catched by listener and it must not handle it initially. Then this exception
     * mapper (which should be chosen as the closest match) will render direct template. If template rendering fails
     * then exception will be redirected directly to error mapper. If it was already error rendering then 404 or
     * 500 will be returned directly.
     * <p>
     * As a side effect, template rendering errors will be impossible to intercept with custom exception mappers.
     *
     * @param event exception event
     * @return true if exception mapper will handle exception (render direct template), false otherwise
     */
    @SuppressWarnings("checkstyle:BooleanExpressionComplexity")
    protected static boolean canHandle(final RequestEvent event) {
        return event.getType() == RequestEvent.Type.ON_EXCEPTION
                && isDirectTemplateRequest()
                // no matched paths
                && event.getUriInfo().getMatchedResources().isEmpty()
                // exception thrown by jersey when path not found
                && event.getException() instanceof NotFoundException
                // its original exception and not failed mapper (other mapper)
                && event.getExceptionCause() == RequestEvent.ExceptionCause.ORIGINAL;
    }

    private static boolean isDirectTemplateRequest() {
        final TemplateContext context = TemplateRedirect.templateContext();
        return context != null && context.isDirectTemplate();
    }
}
