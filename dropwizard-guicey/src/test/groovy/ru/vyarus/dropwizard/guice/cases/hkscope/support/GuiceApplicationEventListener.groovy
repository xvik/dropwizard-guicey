package ru.vyarus.dropwizard.guice.cases.hkscope.support

import org.glassfish.jersey.server.monitoring.ApplicationEvent
import org.glassfish.jersey.server.monitoring.ApplicationEventListener
import org.glassfish.jersey.server.monitoring.RequestEvent
import org.glassfish.jersey.server.monitoring.RequestEventListener
import ru.vyarus.dropwizard.guice.module.installer.feature.jersey.GuiceManaged

import jakarta.ws.rs.ext.Provider

/**
 * @author Vyacheslav Rusakov
 * @since 19.01.2016
 */
@Provider
@GuiceManaged
class GuiceApplicationEventListener implements ApplicationEventListener {

    @Override
    void onEvent(ApplicationEvent event) {

    }

    @Override
    RequestEventListener onRequest(RequestEvent requestEvent) {
        return null
    }
}
