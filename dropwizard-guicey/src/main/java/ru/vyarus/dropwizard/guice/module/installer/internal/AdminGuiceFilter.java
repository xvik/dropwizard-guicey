package ru.vyarus.dropwizard.guice.module.installer.internal;

import com.google.inject.servlet.GuiceFilter;

import jakarta.servlet.*;
import java.io.IOException;

/**
 * Filter is registered on admin context to provide request scope objects support in admin context.
 * {@link GuiceFilter} is tied to single context and can't be initialized directly on both contexts.
 * But filter uses servlet context only to initialize (bound) registered servlet modules. Which makes
 * possible using guice filter gust to managing request scope on admin context (using the same filter
 * instance as on main context).
 * <p>Extra filter class is required to avoid guice filter double initialization.</p>
 *
 * @author Vyacheslav Rusakov
 * @since 03.09.2015
 */
public class AdminGuiceFilter implements Filter {

    private final GuiceFilter filter;

    /**
     * Create admin filter for existing guice filter.
     *
     * @param filter guice filter
     */
    public AdminGuiceFilter(final GuiceFilter filter) {
        this.filter = filter;
    }

    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {
        // not needed
    }

    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain)
            throws IOException, ServletException {
        filter.doFilter(request, response, chain);
    }

    @Override
    public void destroy() {
        // not needed
    }
}
