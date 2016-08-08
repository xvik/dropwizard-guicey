package ru.vyarus.dropwizard.guice.support.web.listeners

import javax.servlet.annotation.WebListener
import javax.servlet.http.HttpSessionEvent
import javax.servlet.http.HttpSessionListener

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
