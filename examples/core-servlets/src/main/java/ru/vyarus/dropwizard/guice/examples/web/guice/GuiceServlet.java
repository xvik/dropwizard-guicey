package ru.vyarus.dropwizard.guice.examples.web.guice;

import ru.vyarus.dropwizard.guice.examples.service.SampleService;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
