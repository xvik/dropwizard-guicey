package ru.vyarus.dropwizard.guice.support.web.servletclash

import jakarta.servlet.annotation.WebServlet
import jakarta.servlet.http.HttpServlet

/**
 * @author Vyacheslav Rusakov
 * @since 08.08.2016
 */
@WebServlet("/sam")
class Servlet1 extends HttpServlet{
}
