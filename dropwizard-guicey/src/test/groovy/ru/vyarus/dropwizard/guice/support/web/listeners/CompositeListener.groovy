package ru.vyarus.dropwizard.guice.support.web.listeners

import jakarta.servlet.ServletContextEvent
import jakarta.servlet.ServletContextListener
import jakarta.servlet.ServletRequestAttributeEvent
import jakarta.servlet.ServletRequestAttributeListener
import jakarta.servlet.annotation.WebListener
import jakarta.servlet.http.HttpSessionEvent
import jakarta.servlet.http.HttpSessionListener

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
