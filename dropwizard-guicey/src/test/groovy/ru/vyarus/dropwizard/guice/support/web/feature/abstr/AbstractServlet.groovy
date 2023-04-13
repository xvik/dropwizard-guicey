package ru.vyarus.dropwizard.guice.support.web.feature.abstr

import jakarta.servlet.annotation.WebServlet
import jakarta.servlet.http.HttpServlet

/**
 * @author Vyacheslav Rusakov 
 * @since 13.10.2014
 */
@WebServlet("/sample")
abstract class AbstractServlet extends HttpServlet {
}
