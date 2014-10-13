package ru.vyarus.dropwizard.guice.support.feature

import com.google.common.base.Preconditions
import com.google.inject.Inject
import ru.vyarus.dropwizard.guice.module.installer.feature.admin.AdminServlet

import javax.servlet.ServletException
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * @author Vyacheslav Rusakov 
 * @since 13.10.2014
 */
@AdminServlet(name = "dummy", patterns = "/dummy")
class DummyAdminServlet extends HttpServlet {

    DummyService service

    @Inject
    DummyAdminServlet(DummyService service) {
        this.service = service
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Preconditions.checkNotNull(service)
        resp.getWriter().write("dispatched")
    }
}
