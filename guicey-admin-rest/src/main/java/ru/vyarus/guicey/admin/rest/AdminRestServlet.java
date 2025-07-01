package ru.vyarus.guicey.admin.rest;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Forwards all requests into jersey context.
 * <p>
 * Marks request with custom attribute {@link #ADMIN_PROPERTY} to indicate admin rest usage.
 * It may be used later to recognize rest origin. For example, {@link AdminResourceFilter} use it to prevent
 * access to admin resources (annotated with {@link AdminResource}) from user context.
 *
 * @author Vyacheslav Rusakov
 * @since 04.08.2015
 */
public class AdminRestServlet extends HttpServlet {
    /**
     * Request attribute name set with 'true' value to distinguish admin rest from user context rest call.
     */
    public static final String ADMIN_PROPERTY = AdminRestServlet.class.getName();

    /**
     * Servlet context.
     */
    private final Servlet restServlet;

    /**
     * @param restServlet dropwizard rest servlet (environment.getJerseyServletContainer())
     */
    public AdminRestServlet(final Servlet restServlet) {
        this.restServlet = restServlet;
    }

    @Override
    protected void service(final HttpServletRequest req, final HttpServletResponse resp)
            throws ServletException, IOException {
        req.setAttribute(ADMIN_PROPERTY, true);
        restServlet.service(req, resp);
    }
}
