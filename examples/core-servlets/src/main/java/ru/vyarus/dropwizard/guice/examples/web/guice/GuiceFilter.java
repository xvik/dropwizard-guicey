package ru.vyarus.dropwizard.guice.examples.web.guice;

import ru.vyarus.dropwizard.guice.examples.service.SampleService;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Vyacheslav Rusakov
 * @since 31.12.2019
 */
@Singleton
public class GuiceFilter extends HttpFilter {

    @Inject
    private SampleService service;

    @Override
    protected void doFilter(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws IOException, ServletException {
        res.getWriter().write(service.filterPart() + " guice ");
        chain.doFilter(req, res);
    }
}
