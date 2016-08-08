package ru.vyarus.dropwizard.guice.support.web.feature

import javax.servlet.*
import javax.servlet.annotation.WebFilter

/**
 * Filter mapped on admin servlet.
 *
 * @author Vyacheslav Rusakov 
 * @since 14.10.2014
 */
@WebFilter(servletNames = ".dummy")
class FilterOnServlet implements Filter {

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
