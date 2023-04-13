package ru.vyarus.dropwizard.guice.debug.renderer.web.support

import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBootstrap
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBundle
import ru.vyarus.dropwizard.guice.module.installer.feature.web.AdminContext

import jakarta.servlet.DispatcherType
import jakarta.servlet.annotation.WebFilter
import jakarta.servlet.annotation.WebServlet
import jakarta.servlet.http.HttpFilter
import jakarta.servlet.http.HttpServlet

/**
 * @author Vyacheslav Rusakov
 * @since 24.10.2019
 */
class UserServletsBundle implements GuiceyBundle {

    @Override
    void initialize(GuiceyBootstrap bootstrap) {
        bootstrap.extensions(
                MainServlet,
                AdminServlet,
                BothServlet,
                AsyncServlet,

                MainFilter,
                TargetServletFilter,
                UnknownTargetServletFilter,
                AdminFilter,
                BothFilter,
                AsyncFilter,
                CustomMappingFilter)
    }

    @WebServlet(urlPatterns = ["/foo", "/bar"], name = "target")
    static class MainServlet extends HttpServlet {}

    @WebServlet(["/fooadmin", "/baradmin"])
    @AdminContext
    static class AdminServlet extends HttpServlet {}

    @WebServlet("/both")
    @AdminContext(andMain = true)
    static class BothServlet extends HttpServlet {}

    @WebServlet(urlPatterns = "/async", asyncSupported = true)
    static class AsyncServlet extends HttpServlet {}

    @WebFilter(["/1/*", "/2/*"])
    static class MainFilter extends HttpFilter {}

    @WebFilter(servletNames = "target")
    static class TargetServletFilter extends HttpFilter {}

    @WebFilter(servletNames = "unknownTarget")
    static class UnknownTargetServletFilter extends HttpFilter {}

    @WebFilter(["/1/*", "/2/*"])
    @AdminContext
    static class AdminFilter extends HttpFilter {}

    @WebFilter("/both/*")
    @AdminContext(andMain = true)
    static class BothFilter extends HttpFilter {}

    @WebFilter(urlPatterns = "/async/*", asyncSupported = true)
    static class AsyncFilter extends HttpFilter {}

    @WebFilter(urlPatterns = "/custom/*", dispatcherTypes = [DispatcherType.ERROR])
    static class CustomMappingFilter extends HttpFilter {}
}
