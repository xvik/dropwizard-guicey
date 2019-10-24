package ru.vyarus.dropwizard.guice.debug.renderer.web.support

import com.google.inject.servlet.ServletModule

import javax.inject.Singleton
import javax.servlet.http.HttpFilter
import javax.servlet.http.HttpServlet

/**
 * @author Vyacheslav Rusakov
 * @since 24.10.2019
 */
class GuiceWebModule extends ServletModule {

    @Override
    protected void configureServlets() {
        bind(GFilter)
        bind(GRegexFilter)
        bind(GServlet)
        bind(GRegexServlet)

        filter("/1/*").through(GFilter)
        filterRegex("/1/abc?/.*").through(GRegexFilter)
        filter("/1/foo").through(new GFilterInstance())

        serve("/2/*").with(GServlet)
        serveRegex("/2/abc?/").with(GRegexServlet)
        serve("/2/foo").with(new GServletInstance())
    }

    @Singleton
    static class GServlet extends HttpServlet {}

    @Singleton
    static class GServletInstance extends HttpServlet {}

    @Singleton
    static class GRegexServlet extends HttpServlet {}

    @Singleton
    static class GFilter extends HttpFilter {}

    @Singleton
    static class GFilterInstance extends HttpFilter {}

    @Singleton
    static class GRegexFilter extends HttpFilter {}
}
