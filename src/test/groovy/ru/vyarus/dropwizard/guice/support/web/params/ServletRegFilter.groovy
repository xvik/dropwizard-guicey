package ru.vyarus.dropwizard.guice.support.web.params

import javax.servlet.*
import javax.servlet.annotation.WebFilter

/**
 * @author Vyacheslav Rusakov
 * @since 08.08.2016
 */
@WebFilter(filterName = "samsam",
        servletNames = "samsam",
        dispatcherTypes = DispatcherType.ERROR)
class ServletRegFilter implements Filter {

    @Override
    void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

    }

    @Override
    void destroy() {

    }
}
