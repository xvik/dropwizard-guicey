package ru.vyarus.dropwizard.guice.support.web

import com.google.common.base.Preconditions
import com.google.inject.Inject
import ru.vyarus.dropwizard.guice.support.feature.DummyService

import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.FilterConfig
import jakarta.servlet.ServletException
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse

/**
 * @author Vyacheslav Rusakov 
 * @since 12.10.2014
 */
@jakarta.inject.Singleton
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
        response.getWriter().append("Sample filter")
    }

    @Override
    void destroy() {
    }
}
