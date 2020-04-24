package ru.vyarus.dropwizard.guice.debug.renderer.guice.support;

import com.google.inject.Singleton;
import com.google.inject.servlet.ServletModule;

import javax.servlet.http.HttpFilter;
import javax.servlet.http.HttpServlet;

/**
 * @author Vyacheslav Rusakov
 * @since 20.08.2019
 */
public class WebModule extends ServletModule {

    @Override
    protected void configureServlets() {
        filter("/1/*").through(WFilter.class);
        serve("/1/foo").with(WServlet.class);

        filter("/2/*").through(new WFilter());
        serve("/2/foo").with(new WServlet());
    }


    @Singleton
    static class WServlet extends HttpServlet {}

    @Singleton
    static class WFilter extends HttpFilter {}
}
