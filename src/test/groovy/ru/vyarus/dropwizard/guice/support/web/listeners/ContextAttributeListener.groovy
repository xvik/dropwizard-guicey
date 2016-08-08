package ru.vyarus.dropwizard.guice.support.web.listeners

import javax.servlet.ServletContextAttributeEvent
import javax.servlet.ServletContextAttributeListener
import javax.servlet.annotation.WebListener

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
