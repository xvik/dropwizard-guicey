package ru.vyarus.dropwizard.guice.support.web.crosscontext

import ru.vyarus.dropwizard.guice.module.installer.feature.web.AdminContext

import javax.servlet.ServletException
import javax.servlet.annotation.WebServlet
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * @author Vyacheslav Rusakov
 * @since 08.08.2016
 */
@AdminContext(andMain = true)
@WebServlet("/crossS")
class CrossContextServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.writer.append("ok")
    }
}
