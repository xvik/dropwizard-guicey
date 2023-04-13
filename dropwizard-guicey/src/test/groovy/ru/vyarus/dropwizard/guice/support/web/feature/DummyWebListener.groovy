package ru.vyarus.dropwizard.guice.support.web.feature

import ru.vyarus.dropwizard.guice.support.feature.DummyService

import jakarta.inject.Inject
import jakarta.servlet.ServletRequestEvent
import jakarta.servlet.ServletRequestListener
import jakarta.servlet.annotation.WebListener

/**
 * @author Vyacheslav Rusakov
 * @since 07.08.2016
 */
@WebListener
class DummyWebListener implements ServletRequestListener {

    @Inject
    DummyWebListener(DummyService service) {
    }

    @Override
    void requestDestroyed(ServletRequestEvent sre) {

    }

    @Override
    void requestInitialized(ServletRequestEvent sre) {

    }
}
