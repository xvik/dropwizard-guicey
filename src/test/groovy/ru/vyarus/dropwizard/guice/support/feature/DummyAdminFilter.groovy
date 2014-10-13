package ru.vyarus.dropwizard.guice.support.feature

import com.google.common.base.Preconditions
import com.google.inject.Inject
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
@AdminFilter(name = "dummy", patterns = "/sample")
class DummyAdminFilter implements Filter{

    DummyService service

    @Inject
    DummyAdminFilter(DummyService service) {
        this.service = service
    }

    @Override
    void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        Preconditions.checkNotNull(service)
        response.getWriter().write("dispatched")
    }

    @Override
    void destroy() {

    }
}
