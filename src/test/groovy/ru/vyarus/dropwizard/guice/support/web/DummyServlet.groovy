package ru.vyarus.dropwizard.guice.support.web

import com.google.common.base.Preconditions
import com.google.inject.Inject
import ru.vyarus.dropwizard.guice.support.feature.DummyService

import javax.servlet.ServletException
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * @author Vyacheslav Rusakov 
 * @since 12.10.2014
 */
@javax.inject.Singleton
class DummyServlet extends HttpServlet {

    DummyService service

    @Inject
    DummyServlet(DummyService service) {
        this.service = service
    }


    DummyServlet() {
        println 'servlet'
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Preconditions.checkNotNull(service)
        resp.getWriter().write("Sample servlet")
    }
}
