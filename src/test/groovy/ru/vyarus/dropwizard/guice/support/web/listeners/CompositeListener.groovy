package ru.vyarus.dropwizard.guice.support.web.listeners

import javax.servlet.ServletContextEvent
import javax.servlet.ServletContextListener
import javax.servlet.ServletRequestAttributeEvent
import javax.servlet.ServletRequestAttributeListener
import javax.servlet.annotation.WebListener
import javax.servlet.http.HttpSessionEvent
import javax.servlet.http.HttpSessionListener

/**
 * @author Vyacheslav Rusakov
 * @since 09.08.2016
 */
@WebListener
class CompositeListener implements ServletContextListener, ServletRequestAttributeListener, HttpSessionListener {

    @Override
    void contextInitialized(ServletContextEvent sce) {

    }

    @Override
    void contextDestroyed(ServletContextEvent sce) {

    }

    @Override
    void attributeAdded(ServletRequestAttributeEvent srae) {

    }

    @Override
    void attributeRemoved(ServletRequestAttributeEvent srae) {

    }

    @Override
    void attributeReplaced(ServletRequestAttributeEvent srae) {

    }

    @Override
    void sessionCreated(HttpSessionEvent se) {

    }

    @Override
    void sessionDestroyed(HttpSessionEvent se) {

    }
}
