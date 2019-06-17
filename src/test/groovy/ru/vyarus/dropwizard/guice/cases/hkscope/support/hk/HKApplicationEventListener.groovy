package ru.vyarus.dropwizard.guice.cases.hkscope.support.hk

import org.glassfish.jersey.server.monitoring.ApplicationEvent
import org.glassfish.jersey.server.monitoring.ApplicationEventListener
import org.glassfish.jersey.server.monitoring.RequestEvent
import org.glassfish.jersey.server.monitoring.RequestEventListener
import ru.vyarus.dropwizard.guice.module.installer.feature.jersey.JerseyManaged

import javax.ws.rs.ext.Provider

/**
 * @author Vyacheslav Rusakov
 * @since 19.01.2016
 */
@Provider
@JerseyManaged
class HKApplicationEventListener implements ApplicationEventListener {

    @Override
    void onEvent(ApplicationEvent event) {

    }

    @Override
    RequestEventListener onRequest(RequestEvent requestEvent) {
        return null
    }
}
