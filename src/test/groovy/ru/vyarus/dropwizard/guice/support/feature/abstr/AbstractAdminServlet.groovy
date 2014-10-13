package ru.vyarus.dropwizard.guice.support.feature.abstr

import ru.vyarus.dropwizard.guice.module.installer.feature.admin.AdminServlet

import javax.servlet.http.HttpServlet

/**
 * @author Vyacheslav Rusakov 
 * @since 13.10.2014
 */
@AdminServlet(name = "sample", patterns = "/sample")
abstract class AbstractAdminServlet extends HttpServlet {
}
