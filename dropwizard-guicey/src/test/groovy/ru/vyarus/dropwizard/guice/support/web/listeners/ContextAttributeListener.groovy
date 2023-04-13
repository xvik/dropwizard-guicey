package ru.vyarus.dropwizard.guice.support.web.listeners

import jakarta.servlet.ServletContextAttributeEvent
import jakarta.servlet.ServletContextAttributeListener
import jakarta.servlet.annotation.WebListener

/**
 * @author Vyacheslav Rusakov
 * @since 09.08.2016
 */
@WebListener
class ContextAttributeListener implements ServletContextAttributeListener {
    @Override
    void attributeAdded(ServletContextAttributeEvent event) {

    }

    @Override
    void attributeRemoved(ServletContextAttributeEvent event) {

    }

    @Override
    void attributeReplaced(ServletContextAttributeEvent event) {

    }
}
