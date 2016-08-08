package ru.vyarus.dropwizard.guice.support.web.params

import javax.servlet.*
import javax.servlet.annotation.WebFilter
import javax.servlet.annotation.WebInitParam

/**
 * @author Vyacheslav Rusakov
 * @since 08.08.2016
 */
@WebFilter(urlPatterns = "/dummy",
        asyncSupported = true,
        filterName = 'dummy',
        initParams = [
                @WebInitParam(name = "par1", value = "val1"),
                @WebInitParam(name = "par2", value = "val2")
        ])
class InitParamsFilter implements Filter {

    @Override
    void init(FilterConfig filterConfig) throws ServletException {
        assert filterConfig.getFilterName() == "dummy"
        assert filterConfig.getInitParameter("par1") == "val1"
        assert filterConfig.getInitParameter("par2") == "val2"
    }

    @Override
    void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
    }

    @Override
    void destroy() {

    }
}
