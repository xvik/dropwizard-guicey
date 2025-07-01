package ru.vyarus.guicey.gsp.app.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

/**
 * Request wrapper used to prefix app name into original request url during redirection to rest.
 * For example, original url like '/some/url/' transformed into '{app}/some/url' where {app} is server pages
 * application registration name.
 *
 * @author Vyacheslav Rusakov
 * @since 18.01.2019
 */
public class TemplateRequest extends HttpServletRequestWrapper {

    private final String path;
    private final String context;
    private final String mapping;

    /**
     * Create template request.
     *
     * @param request request object
     * @param path    path
     * @param context context
     * @param mapping servlet mapping path
     */
    public TemplateRequest(
            final HttpServletRequest request,
            final String path,
            final String context,
            final String mapping) {
        super(request);
        this.path = path;
        this.context = context;
        this.mapping = mapping;
    }

    @Override
    public String getRequestURI() {
        return path;
    }

    @Override
    public StringBuffer getRequestURL() {
        // it's not efficient because original buffer is overridden, but at least correct
        final String originalPath = super.getRequestURI();
        final String res = super.getRequestURL().toString()
                .replace(originalPath, path);
        return new StringBuffer(res);
    }

    // overrides below are required for proper handling inside admin context (with flat mapping)

    @Override
    public String getContextPath() {
        // (main) context mapping path
        return context;
    }

    @Override
    public String getServletPath() {
        // (main) servlet mapping path
        return mapping;
    }
}
