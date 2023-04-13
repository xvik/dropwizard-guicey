package ru.vyarus.dropwizard.guice.support.web.session

import jakarta.servlet.annotation.WebListener
import jakarta.servlet.http.HttpSessionEvent
import jakarta.servlet.http.HttpSessionIdListener

/**
 * @author Vyacheslav Rusakov
 * @since 08.08.2016
 */
@WebListener
class SessionListener implements HttpSessionIdListener {

    @Override
    void sessionIdChanged(HttpSessionEvent event, String oldSessionId) {

    }
}
