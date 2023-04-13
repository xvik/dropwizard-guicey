package ru.vyarus.dropwizard.guice.support.web.listeners

import jakarta.servlet.annotation.WebListener
import jakarta.servlet.http.HttpSessionEvent
import jakarta.servlet.http.HttpSessionIdListener

/**
 * @author Vyacheslav Rusakov
 * @since 09.08.2016
 */
@WebListener
class SessionIdListener implements HttpSessionIdListener {

    @Override
    void sessionIdChanged(HttpSessionEvent event, String oldSessionId) {

    }
}
