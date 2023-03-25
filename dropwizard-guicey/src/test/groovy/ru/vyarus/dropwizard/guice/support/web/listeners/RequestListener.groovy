package ru.vyarus.dropwizard.guice.support.web.listeners

import ru.vyarus.dropwizard.guice.module.installer.feature.web.AdminContext

import javax.servlet.ServletRequestEvent
import javax.servlet.ServletRequestListener
import javax.servlet.annotation.WebListener

/**
 * @author Vyacheslav Rusakov
 * @since 09.08.2016
 */
@AdminContext(andMain = true)
@WebListener
class RequestListener implements ServletRequestListener {

    @Override
    void requestDestroyed(ServletRequestEvent sre) {

    }

    @Override
    void requestInitialized(ServletRequestEvent sre) {

    }
}
