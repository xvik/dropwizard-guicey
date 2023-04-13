package ru.vyarus.dropwizard.guice.support.web.crosscontext

import ru.vyarus.dropwizard.guice.module.installer.feature.web.AdminContext

import jakarta.servlet.ServletRequestEvent
import jakarta.servlet.ServletRequestListener
import jakarta.servlet.annotation.WebListener
import jakarta.servlet.http.HttpServletRequest

/**
 * @author Vyacheslav Rusakov
 * @since 08.08.2016
 */
@AdminContext(andMain = true)
@WebListener
class CrossContextListener implements ServletRequestListener {

    static Set<Integer> ports = []
    static Set<String> contexts = []

    @Override
    void requestDestroyed(ServletRequestEvent sre) {

    }

    @Override
    void requestInitialized(ServletRequestEvent sre) {
        ports.add(sre.servletRequest.localPort)
        contexts.add((sre.servletRequest as HttpServletRequest).contextPath ?: '/')
    }
}
