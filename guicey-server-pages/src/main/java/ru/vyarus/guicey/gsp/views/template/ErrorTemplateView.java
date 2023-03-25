package ru.vyarus.guicey.gsp.views.template;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Throwables;
import ru.vyarus.guicey.gsp.app.filter.redirect.ErrorRedirect;

import javax.annotation.Nullable;
import javax.ws.rs.WebApplicationException;
import java.nio.charset.Charset;

/**
 * Error template rendering model. Must be used for error pages (registered in main bundle
 * {@link ru.vyarus.guicey.gsp.app.ServerPagesAppBundle.AppBuilder#errorPage(int, String)}). Provides access to
 * original error and original (failed) page url (note that it can't be taken from current context request because
 * error page is rendered as usual page and so current request will contain error page url only).
 * <p>
 * There are 3 possible exception cases:
 * <ul>
 * <li>{@link ru.vyarus.guicey.gsp.app.filter.AssetError} for static asset serving error</li>
 * <li>{@link ru.vyarus.guicey.gsp.app.rest.support.TemplateRestCodeError} for error code directly returned
 * from rest without throwing exception (e.g. {@code Response.status(404).build()})</li>
 * <li>Any exception extending {@link WebApplicationException}. This is exactly exception thrown in rest method
 * or simple exception wrapped with {@link WebApplicationException}.</li>
 * </ul>
 * Note that not "real" exception types implement {@link ru.vyarus.guicey.gsp.app.util.TracelessException} to
 * simplify detection of exception with meaningful trace.
 * <p>
 * Note: in contrast to usual templates, during error page rendering context request will contain error page path,
 * but original url is still available with {@link #getErroredUrl()}.
 *
 * @author Vyacheslav Rusakov
 * @since 31.01.2019
 */
public class ErrorTemplateView extends TemplateView {

    private final WebApplicationException error;
    private final String erroredUrl;

    public ErrorTemplateView() {
        this(null);
    }

    public ErrorTemplateView(@Nullable final String templatePath) {
        this(templatePath, null);
    }

    public ErrorTemplateView(@Nullable final String templatePath, @Nullable final Charset charset) {
        super(templatePath, charset);
        this.error = ErrorRedirect.getContextError();
        this.erroredUrl = ErrorRedirect.getContextErrorOriginalUrl();
    }

    /**
     * Returns exception object only during rendering of configured error page
     * (from {@link ru.vyarus.guicey.gsp.app.ServerPagesAppBundle.AppBuilder#errorPage(int, String)}).
     * For all other cases (from error pages) method is useless.
     *
     * @return exception object or null (for normal template rendering)
     */
    @JsonIgnore
    public WebApplicationException getError() {
        return error;
    }

    /**
     * Shortcut for {@code getError().getResponse().getStatus()}. Shortcut created because direct expression
     * can't be used in freemarker expression.
     *
     * @return status code from context error or -1 if no context error
     * @see #getError()
     */
    @JsonIgnore
    public int getErrorCode() {
        return error != null ? error.getResponse().getStatus() : -1;
    }

    /**
     * Method intended to be used in very simple error pages in order to quickly show stacktrace.
     * <p>
     * Note that in case of direct error code return (404, 500 etc) exception will be "empty"
     * (exception instance will be created but not thrown).
     *
     * @return current stacktrace as string or null if no context error
     * @see #getError()
     */
    @JsonIgnore
    public String getErrorTrace() {
        return error != null ? Throwables.getStackTraceAsString(error) : null;
    }

    /**
     * Note returned only uri part ({@code request.getRequestURI()}).
     *
     * @return page url cause redirect to error page
     */
    public String getErroredUrl() {
        return erroredUrl;
    }
}
