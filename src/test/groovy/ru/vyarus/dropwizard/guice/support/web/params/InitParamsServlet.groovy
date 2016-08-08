package ru.vyarus.dropwizard.guice.support.web.params

import javax.servlet.ServletConfig
import javax.servlet.ServletException
import javax.servlet.annotation.WebInitParam
import javax.servlet.annotation.WebServlet
import javax.servlet.http.HttpServlet

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
