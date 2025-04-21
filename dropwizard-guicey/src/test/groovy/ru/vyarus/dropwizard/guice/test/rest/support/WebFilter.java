package ru.vyarus.dropwizard.guice.test.rest.support;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * @author Vyacheslav Rusakov
 * @since 22.02.2025
 */
@jakarta.servlet.annotation.WebFilter("/*")
public class WebFilter extends HttpFilter {

    public boolean called = false;

    @Override
    protected void doFilter(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws IOException, ServletException {
        called = true;
        super.doFilter(req, res, chain);
    }
}
