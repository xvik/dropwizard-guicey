package ru.vyarus.dropwizard.guice.support.web.listeners

import javax.servlet.ServletRequestAttributeEvent
import javax.servlet.ServletRequestAttributeListener
import javax.servlet.annotation.WebListener

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
