package ru.vyarus.guicey.gsp.app.filter.redirect;

import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vyarus.dropwizard.guice.module.installer.util.PathUtils;
import ru.vyarus.guicey.gsp.app.filter.AssetError;
import ru.vyarus.guicey.gsp.app.rest.support.TemplateRestCodeError;
import ru.vyarus.guicey.spa.filter.SpaUtils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.WebApplicationException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Redirects response error to the configured error page
 * ({@link ru.vyarus.guicey.gsp.app.ServerPagesAppBundle.AppBuilder#errorPage(String)}).
 * Only response codes &gt;= 400 (errors) are handled, everything else considered as normal flow.
 * <p>
 * When SPA support is enabled, also intercept all 404 errors and checks if it could be SPA route (and do home redirect
 * instead of error).
 * <p>
 * Asset errors are intercepted directly inside {@link ru.vyarus.guicey.gsp.app.filter.ServerPagesFilter}.
 * Rest errors are intercepted with {@link ru.vyarus.guicey.gsp.app.rest.support.TemplateExceptionListener}
 * and {@link ru.vyarus.guicey.gsp.app.rest.support.TemplateErrorResponseFilter}.
 *
 * @author Vyacheslav Rusakov
 * @since 07.12.2018
 */
public class ErrorRedirect {
    /**
     * Special code for default error page registration
     * ({@link ru.vyarus.guicey.gsp.app.ServerPagesAppBundle.AppBuilder#errorPage(String)}).
     */
    public static final int DEFAULT_ERROR_PAGE = -1;
    /**
     * Code 400.
     */
    public static final int CODE_400 = 400;

    private static final ThreadLocal<ErrorContext> CONTEXT_ERROR = new ThreadLocal<>();
    private final Logger logger = LoggerFactory.getLogger(ErrorRedirect.class);

    private final Map<Integer, String> errorPages;
    private final SpaSupport spa;

    /**
     * Create error redirect.
     *
     * @param appMapping application mapping path
     * @param pages      error pages
     * @param spa        SPA support
     */
    public ErrorRedirect(final String appMapping,
                         final Map<Integer, String> pages,
                         final SpaSupport spa) {
        // copy for modifications
        this.errorPages = new HashMap<>(pages);
        this.spa = spa;
        // normalize paths to be absolute
        for (int code : pages.keySet()) {
            errorPages.put(code, PathUtils.path(appMapping, errorPages.get(code)));
        }
    }

    /**
     * Try to redirect error to configured error page.
     *
     * @param request   request
     * @param response  response
     * @param exception error (either simple wrapping for error code or complete stacktrace from rest)
     * @return true if error page found and false if no page configured (no special handling required)
     */
    public boolean redirect(final HttpServletRequest request,
                            final HttpServletResponse response,
                            final WebApplicationException exception) {
        return spa.redirect(request, response, exception.getResponse().getStatus())
                || (SpaUtils.isHtmlRequest(request) && doRedirect(request, response, exception));
    }

    /**
     * Note: method is not supposed to be used directly as error object is directly available in model:
     * {@link ru.vyarus.guicey.gsp.views.template.ErrorTemplateView#getError()}.
     *
     * @return thread bound exception to use in error page rendering or null if no error bound
     */
    public static WebApplicationException getContextError() {
        return getContext().exception;
    }

    /**
     * Returned string is {@code request.getRequestURI()} from original request.
     *
     * @return url of original page (before redirect to error page)
     */
    public static String getContextErrorOriginalUrl() {
        return getContext().originalUrl;
    }

    /**
     * @return true indicate error page rendering, false in all other cases
     */
    public static boolean hasContextError() {
        return CONTEXT_ERROR.get() != null;
    }

    private String selectErrorPage(final WebApplicationException exception) {
        final int status = exception.getResponse().getStatus();
        if (status >= CODE_400) {
            final String res = errorPages.get(status);
            return res == null ? errorPages.get(DEFAULT_ERROR_PAGE) : res;
        }
        return null;
    }

    private boolean doRedirect(final HttpServletRequest request,
                               final HttpServletResponse response,
                               final WebApplicationException exception) {
        // special case: error during error page rendering (original error code returned)
        return handleErrorRenderingError(exception, request, response)
                // redirect to error page (note it will be completely new processing cycle, starting from filter)
                || (CONTEXT_ERROR.get() == null && handleErrorRedirect(exception, request, response));
    }

    private boolean handleErrorRenderingError(final WebApplicationException exception,
                                              final HttpServletRequest request,
                                              final HttpServletResponse response) {
        final ErrorContext context = CONTEXT_ERROR.get();
        if (context != null && !context.processed) {
            // logged as debug, because most likely dropwizard will log it (hiding duplicate with debug level)
            logger.debug("Error page '" + request.getRequestURI() + "' processing error", exception);
            onUnexpectedError(request.getRequestURI(), context.originalUrl,
                    context.exception.getResponse().getStatus(), context.exception, response);
            context.processed = true;
            // return true to indicate "processed" response
            return true;
        }
        return false;
    }

    private boolean handleErrorRedirect(final WebApplicationException exception,
                                        final HttpServletRequest request,
                                        final HttpServletResponse response) {
        final String path = selectErrorPage(exception);
        if (path != null && !response.isCommitted()) {
            logger.debug("Redirecting failed '{}' request to '{}' error page", request.getRequestURI(), path);
            // to be able to access exception in error view
            final ErrorContext context = new ErrorContext(exception, request);
            CONTEXT_ERROR.set(context);
            try {
                // clear error status
                response.reset();
                request.getRequestDispatcher(path).forward(request, response);
                // if error page rendering will fail, forward will not throw an exception, so this message
                // will be incorrect
                if (!context.processed) {
                    logger.info("Serving error page '{}' instead of '{}' response error {}",
                            path, request.getRequestURL(), exception.getResponse().getStatus());
                }
            } catch (Exception ex) {
                onUnexpectedError(path, request.getRequestURI(), exception.getResponse().getStatus(),
                        exception, response);
            } finally {
                CONTEXT_ERROR.remove();
            }
            return true;
        }
        return false;
    }

    private void onUnexpectedError(final String errorPage,
                                   final String originalPage,
                                   final int code,
                                   final Exception originalException,
                                   final HttpServletResponse response) {
        final String ex;
        if (originalException instanceof AssetError) {
            ex = "asset error";
        } else if (originalException instanceof TemplateRestCodeError) {
            ex = "direct rest status";
        } else {
            ex = "rest exception";
        }
        // note exception is not logged here because most likely dropwizard will log rest exception itself
        logger.error(String.format("Failed to serve error page '%s' for request '%s' instead of %s. "
                        + "Error code %s will be returned instead of error page.",
                errorPage, originalPage, ex, code));
        try {
            response.setStatus(CONTEXT_ERROR.get().exception.getResponse().getStatus());
            // commit response so jersey will not try to handle it
            response.flushBuffer();
        } catch (IOException e) {
            logger.error("Error processing response", e);
        }
    }

    private static ErrorContext getContext() {
        return Preconditions.checkNotNull(CONTEXT_ERROR.get(), "No context error");
    }


    /**
     * Error context object.
     */
    @SuppressWarnings("checkstyle:VisibilityModifier")
    private static class ErrorContext {
        /**
         * Exception instance (error leading to error page).
         */
        protected WebApplicationException exception;
        /**
         * Original request uri (stored because during error page rendering it will be unreachable).
         */
        protected String originalUrl;
        /**
         * Processing marker used to prevent multiple error handling.
         */
        protected boolean processed;

        protected ErrorContext(final WebApplicationException exception, final HttpServletRequest originalRequest) {
            this.exception = exception;
            this.originalUrl = originalRequest.getRequestURI();
        }
    }
}
