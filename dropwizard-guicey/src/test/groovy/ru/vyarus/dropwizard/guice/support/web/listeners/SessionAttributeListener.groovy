package ru.vyarus.dropwizard.guice.support.web.listeners

import jakarta.servlet.annotation.WebListener
import jakarta.servlet.http.HttpSessionAttributeListener
import jakarta.servlet.http.HttpSessionBindingEvent

/**
 * @author Vyacheslav Rusakov
 * @since 09.08.2016
 */
@WebListener
class SessionAttributeListener implements HttpSessionAttributeListener {

    @Override
    void attributeAdded(HttpSessionBindingEvent event) {

    }

    @Override
    void attributeRemoved(HttpSessionBindingEvent event) {

    }

    @Override
    void attributeReplaced(HttpSessionBindingEvent event) {

    }
}
