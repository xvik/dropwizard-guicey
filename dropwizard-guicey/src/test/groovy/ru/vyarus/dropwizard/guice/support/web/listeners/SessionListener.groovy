package ru.vyarus.dropwizard.guice.support.web.listeners

import jakarta.servlet.annotation.WebListener
import jakarta.servlet.http.HttpSessionEvent
import jakarta.servlet.http.HttpSessionListener

/**
 * @author Vyacheslav Rusakov
 * @since 09.08.2016
 */
@WebListener
class SessionListener implements HttpSessionListener {

    @Override
    void sessionCreated(HttpSessionEvent se) {

    }

    @Override
    void sessionDestroyed(HttpSessionEvent se) {

    }
}
