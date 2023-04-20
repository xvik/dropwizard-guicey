package ru.vyarus.dropwizard.guice.support.web.session

import javax.servlet.annotation.WebListener
import javax.servlet.http.HttpSessionEvent
import javax.servlet.http.HttpSessionIdListener

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
