package ru.vyarus.dropwizard.guice.examples.web.guice;

import ru.vyarus.dropwizard.guice.examples.service.SampleService;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Vyacheslav Rusakov
 * @since 31.12.2019
 */
@Singleton
public class GuiceServlet extends HttpServlet {

    @Inject
    private SampleService service;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.getWriter().write(service.servletPart() + " guice");
        resp.flushBuffer();
    }
}
