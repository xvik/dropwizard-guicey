package ru.vyarus.dropwizard.guice.support.web.feature

import com.google.common.base.Preconditions
import ru.vyarus.dropwizard.guice.support.feature.DummyService

import javax.inject.Inject
import javax.servlet.ServletException
import javax.servlet.annotation.WebServlet
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * @author Vyacheslav Rusakov 
 * @since 13.10.2014
 */
@WebServlet("/dummy")
class DummyServlet extends HttpServlet {

    DummyService service

    @Inject
    DummyServlet(DummyService service) {
        this.service = service
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Preconditions.checkNotNull(service)
        resp.getWriter().write("dispatched")
    }
}
