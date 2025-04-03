package ru.vyarus.dropwizard.guice.test.jupiter.setup.rest.support;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * @author Vyacheslav Rusakov
 * @since 22.02.2025
 */
@javax.servlet.annotation.WebFilter("/*")
public class WebFilter extends HttpFilter {

    public boolean called = false;

    @Override
    protected void doFilter(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws IOException, ServletException {
        called = true;
        super.doFilter(req, res, chain);
    }
}
