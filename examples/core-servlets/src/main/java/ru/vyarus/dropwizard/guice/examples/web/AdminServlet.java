package ru.vyarus.dropwizard.guice.examples.web;

import ru.vyarus.dropwizard.guice.examples.service.SampleService;
import ru.vyarus.dropwizard.guice.module.installer.feature.web.AdminContext;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Vyacheslav Rusakov
 * @since 31.12.2019
 */
@Singleton
@WebServlet("/admin")
@AdminContext
public class AdminServlet extends HttpServlet {

    @Inject
    SampleService service;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.getWriter().write(service.servletPart() + " admin");
        resp.flushBuffer();
    }
}
