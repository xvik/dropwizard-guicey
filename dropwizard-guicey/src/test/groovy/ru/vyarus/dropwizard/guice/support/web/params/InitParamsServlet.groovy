package ru.vyarus.dropwizard.guice.support.web.params

import jakarta.servlet.ServletConfig
import jakarta.servlet.ServletException
import jakarta.servlet.annotation.WebInitParam
import jakarta.servlet.annotation.WebServlet
import jakarta.servlet.http.HttpServlet

/**
 * @author Vyacheslav Rusakov
 * @since 08.08.2016
 */
@WebServlet(urlPatterns = '/sample',
        name = "sample",
        asyncSupported = true,
        initParams = [
                @WebInitParam(name = "par1", value = "val1"),
                @WebInitParam(name = "par2", value = "val2")
        ])
class InitParamsServlet extends HttpServlet {

    @Override
    void init(ServletConfig config) throws ServletException {
        super.init(config)
        assert config.servletName == "sample"
        assert config.getInitParameter("par1") == "val1"
        assert config.getInitParameter("par2") == "val2"
    }
}
