package ru.vyarus.dropwizard.guice.examples.web;

import ru.vyarus.dropwizard.guice.examples.service.SampleService;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Vyacheslav Rusakov
 * @since 31.12.2019
 */
@WebFilter("/sample/*")
@Singleton
public class SampleFilter extends HttpFilter {

    @Inject
    SampleService service;

    @Override
    protected void doFilter(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws IOException, ServletException {
        res.getWriter().write(service.filterPart() + " ");
        chain.doFilter(req, res);
    }
}
