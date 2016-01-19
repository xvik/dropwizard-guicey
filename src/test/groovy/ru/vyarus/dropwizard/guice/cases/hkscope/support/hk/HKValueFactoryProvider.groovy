package ru.vyarus.dropwizard.guice.cases.hkscope.support.hk

import org.glassfish.hk2.api.Factory
import org.glassfish.jersey.server.model.Parameter
import org.glassfish.jersey.server.spi.internal.ValueFactoryProvider
import ru.vyarus.dropwizard.guice.module.installer.feature.jersey.HK2Managed

import javax.ws.rs.ext.Provider

/**
 * @author Vyacheslav Rusakov
 * @since 19.01.2016
 */
@Provider
@HK2Managed
class HKValueFactoryProvider implements ValueFactoryProvider {

    @Override
    Factory<?> getValueFactory(Parameter parameter) {
        return null
    }

    @Override
    ValueFactoryProvider.PriorityType getPriority() {
        return ValueFactoryProvider.Priority.LOW
    }
}
