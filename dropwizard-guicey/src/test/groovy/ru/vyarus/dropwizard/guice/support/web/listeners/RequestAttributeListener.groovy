package ru.vyarus.dropwizard.guice.support.web.listeners

import jakarta.servlet.ServletRequestAttributeEvent
import jakarta.servlet.ServletRequestAttributeListener
import jakarta.servlet.annotation.WebListener

/**
 * @author Vyacheslav Rusakov
 * @since 09.08.2016
 */
@WebListener
class RequestAttributeListener implements ServletRequestAttributeListener {

    @Override
    void attributeAdded(ServletRequestAttributeEvent srae) {

    }

    @Override
    void attributeRemoved(ServletRequestAttributeEvent srae) {

    }

    @Override
    void attributeReplaced(ServletRequestAttributeEvent srae) {

    }
}
