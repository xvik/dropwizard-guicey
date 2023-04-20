package ru.vyarus.dropwizard.guice.support.web

import com.google.inject.servlet.ServletModule

/**
 * @author Vyacheslav Rusakov 
 * @since 12.10.2014
 */
class WebModule extends ServletModule {

    @Override
    protected void configureServlets() {
        bind(DummyFilter)
        bind(DummyServlet)

        filter('/dummyFilter').through(DummyFilter)
        serve('/dummyServlet').with(DummyServlet)
    }
}
