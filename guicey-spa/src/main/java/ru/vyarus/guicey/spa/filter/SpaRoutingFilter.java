package ru.vyarus.guicey.spa.filter;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.regex.Pattern;

/**
 * Filter must be mapped to assets servlet, serving spa application.
 * Bypass all calls to servlet, but if servlet returns 404, tries to redirect to application main page.
 * <p>
 * This is important to properly handle html5 client routing (without hashbang).
 * <p>
 * In order to route, filter checks request accept header: if it's compatible with "text/html" - routing is performed.
 * If not, 404 error sent. Also, regex pattern is used to prevent routing (for example, for html templates).
 * This is important for all other assets, which absence must be indicated.
 *
 * @author Vyacheslav Rusakov
 * @since 02.04.2017
 */
public class SpaRoutingFilter implements Filter {

    private final String target;
    private final Pattern noRedirect;

    public SpaRoutingFilter(final String target, final String noRedirectRegex) {
        this.target = target;
        noRedirect = Pattern.compile(noRedirectRegex);
    }

    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {
        // not needed
    }

    @Override
    public void doFilter(final ServletRequest servletRequest,
                         final ServletResponse servletResponse,
                         final FilterChain chain) throws IOException, ServletException {

        final HttpServletRequest req = (HttpServletRequest) servletRequest;
        final HttpServletResponse resp = (HttpServletResponse) servletResponse;

        if (SpaUtils.isRootPage(req.getRequestURI(), target)) {
            // direct call for index (no need to redirect)
            SpaUtils.noCache(resp);
            chain.doFilter(req, resp);
        } else {
            checkRedirect(req, resp, chain);
        }
    }

    @Override
    public void destroy() {
        // not needed
    }

    private void checkRedirect(final HttpServletRequest req,
                               final HttpServletResponse resp,
                               final FilterChain chain) throws IOException, ServletException {
        // wrap request to intercept errors
        chain.doFilter(req, resp);

        final int error = resp.getStatus();

        if (error != HttpServletResponse.SC_NOT_FOUND) {
            // nothing to do
            return;
        }

        if (SpaUtils.isSpaRoute(req, noRedirect)) {
            // redirect to root
            SpaUtils.doRedirect(req, resp, target);
        }
    }
}
