package ru.vyarus.dropwizard.guice.support.feature.abstr

import ru.vyarus.dropwizard.guice.module.installer.feature.admin.AdminFilter

import javax.servlet.Filter
import javax.servlet.FilterChain
import javax.servlet.FilterConfig
import javax.servlet.ServletException
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse

/**
 * @author Vyacheslav Rusakov 
 * @since 13.10.2014
 */
@AdminFilter(name = "sample", patterns = "/sample")
abstract class AbstractAdminFilter implements Filter{
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
