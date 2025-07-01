package ru.vyarus.guicey.gsp.app.rest.support;

import org.glassfish.jersey.server.monitoring.ApplicationEvent;
import org.glassfish.jersey.server.monitoring.ApplicationEventListener;
import org.glassfish.jersey.server.monitoring.RequestEvent;
import org.glassfish.jersey.server.monitoring.RequestEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vyarus.guicey.gsp.app.filter.redirect.TemplateRedirect;
import ru.vyarus.guicey.gsp.views.template.TemplateContext;

import jakarta.inject.Singleton;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.ext.Provider;

/**
 * Application listener for template processing exceptions detection. Listener use request listener for template
 * processing templates in order to detect exceptions.
 * <p>
 * This listener doesn't detect direct not OK status return (e.g. {@code Response.status(404).build()}), but
 * {@link TemplateErrorResponseFilter} will handle such cases.
 *
 * @author Vyacheslav Rusakov
 * @since 24.01.2019
 */
@Provider
@Singleton
public class TemplateExceptionListener implements ApplicationEventListener {
    /**
     * Quote.
     */
    public static final String QUOTE = "'";
    // use single instance
    private final RequestListener listener = new RequestListener();

    @Override
    public void onEvent(final ApplicationEvent event) {
        // not needed
    }

    @Override
    public RequestEventListener onRequest(final RequestEvent requestEvent) {
        // apply request audit only under templates processing
        return TemplateRedirect.templateContext() != null ? listener : null;
    }

    /**
     * Jersey request listener used to detect exceptions.
     */
    private static final class RequestListener implements RequestEventListener {
        private final Logger logger = LoggerFactory.getLogger(TemplateExceptionListener.class);

        @Override
        public void onEvent(final RequestEvent event) {
            // event may be called multiple times, but redirect will detect it and do nothing
            if (event.getType() == RequestEvent.Type.ON_EXCEPTION
                    && !DirectTemplateExceptionMapper.canHandle(event)) {
                final Throwable exception = event.getException();
                if (!(exception instanceof WebApplicationException)) {
                    // most likely unexpected exception (dropwizard will log this stacktrace again)
                    logger.error("Exception processing view rest path '"
                            + event.getUriInfo().getPath() + QUOTE, exception);
                } else if (logger.isDebugEnabled()) {
                    logger.debug("View rest processing exception detected for path '"
                            + event.getUriInfo().getPath() + QUOTE, exception);
                }

                // immediately perform redirect to error page (or do nothing)
                TemplateContext.getInstance().redirectError(exception);
            }
        }
    }
}
