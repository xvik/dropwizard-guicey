package ru.vyarus.dropwizard.guice.support.web.feature

import com.google.common.base.Preconditions
import ru.vyarus.dropwizard.guice.support.feature.DummyService

import javax.inject.Inject
import javax.servlet.*
import javax.servlet.annotation.WebFilter

/**
 * @author Vyacheslav Rusakov 
 * @since 13.10.2014
 */
@WebFilter("/sample")
class DummyFilter implements Filter {

    DummyService service

    @Inject
    DummyFilter(DummyService service) {
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
