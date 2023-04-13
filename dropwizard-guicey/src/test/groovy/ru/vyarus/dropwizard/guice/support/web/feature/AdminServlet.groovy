package ru.vyarus.dropwizard.guice.support.web.feature

import ru.vyarus.dropwizard.guice.module.installer.feature.web.AdminContext

import jakarta.servlet.ServletException
import jakarta.servlet.annotation.WebServlet
import jakarta.servlet.http.HttpServlet
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse

/**
 * @author Vyacheslav Rusakov
 * @since 08.08.2016
 */
@AdminContext
@WebServlet("/adminServlet")
class AdminServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.getWriter().write("admin")
    }
}
