package ru.vyarus.dropwizard.guice.support.web.crosscontext

import ru.vyarus.dropwizard.guice.module.installer.feature.web.AdminContext

import jakarta.servlet.*
import jakarta.servlet.annotation.WebFilter

/**
 * @author Vyacheslav Rusakov
 * @since 08.08.2016
 */
@AdminContext(andMain = true)
@WebFilter("/crossF")
class CrossContextFilter implements Filter {

    @Override
    void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        response.writer.append("ok")
    }

    @Override
    void destroy() {

    }
}
