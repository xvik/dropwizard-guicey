package ru.vyarus.dropwizard.guice.support.web.feature

import ru.vyarus.dropwizard.guice.module.installer.feature.web.AdminContext

import javax.servlet.Filter
import javax.servlet.FilterChain
import javax.servlet.FilterConfig
import javax.servlet.ServletException
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.annotation.WebFilter

/**
 * @author Vyacheslav Rusakov
 * @since 08.08.2016
 */
@AdminContext
@WebFilter("/adminFilter")
class AdminFilter implements Filter {

    @Override
    void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    void doFilter(ServletRequest request, ServletResponse resp, FilterChain chain) throws IOException, ServletException {
        resp.getWriter().write("admin")
    }

    @Override
    void destroy() {

    }
}
