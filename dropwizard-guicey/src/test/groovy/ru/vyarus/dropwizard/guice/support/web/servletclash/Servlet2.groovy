package ru.vyarus.dropwizard.guice.support.web.servletclash

import javax.servlet.annotation.WebServlet
import javax.servlet.http.HttpServlet

/**
 * @author Vyacheslav Rusakov
 * @since 08.08.2016
 */
@WebServlet("/sam")
class Servlet2 extends HttpServlet {
}
