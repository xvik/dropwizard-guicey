package ru.vyarus.dropwizard.guice.support.web

import ru.vyarus.dropwizard.guice.module.installer.feature.admin.AdminFilter

import javax.servlet.*

/**
 * Filter mapped on admin servlet.
 *
 * @author Vyacheslav Rusakov 
 * @since 14.10.2014
 */
@AdminFilter(name = "aroundDummy", servlets = "dummy")
class AdminFilterOnServlet implements Filter {

    @Override
    void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        chain.doFilter(request, response);
        response.getWriter().write(" addition")
    }

    @Override
    void destroy() {

    }
}
