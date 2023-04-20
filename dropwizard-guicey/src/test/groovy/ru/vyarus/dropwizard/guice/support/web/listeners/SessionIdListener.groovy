package ru.vyarus.dropwizard.guice.support.web.listeners

import javax.servlet.annotation.WebListener
import javax.servlet.http.HttpSessionEvent
import javax.servlet.http.HttpSessionIdListener

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
