package ru.vyarus.dropwizard.guice.support.web

import com.google.common.base.Preconditions
import com.google.inject.Inject
import ru.vyarus.dropwizard.guice.support.feature.DummyService

import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServlet
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse

/**
 * @author Vyacheslav Rusakov 
 * @since 12.10.2014
 */
@jakarta.inject.Singleton
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
