package ru.vyarus.dropwizard.guice.support.provider

import com.sun.jersey.api.core.HttpContext
import com.sun.jersey.core.spi.component.ComponentContext
import com.sun.jersey.core.spi.component.ComponentScope
import com.sun.jersey.server.impl.inject.AbstractHttpContextInjectable
import com.sun.jersey.spi.inject.Injectable
import com.sun.jersey.spi.inject.InjectableProvider
import ru.vyarus.dropwizard.guice.support.feature.CustomFeature

import javax.ws.rs.core.Context
import javax.ws.rs.ext.Provider
import java.lang.reflect.Type

/**
 * Sample injectable provider for CustomFeature type.
 *
 * @author Vyacheslav Rusakov 
 * @since 09.10.2014
 */
@Provider
class CustomFeatureInjectableProvider extends AbstractHttpContextInjectable<CustomFeature>
        implements InjectableProvider<Context, Type> {
    static int creationCounter = 0
    static int callCounter = 0

    CustomFeatureInjectableProvider() {
        creationCounter++
    }

    @Override
    ComponentScope getScope() {
        // value ignored
        return ComponentScope.Singleton
    }

    @Override
    Injectable getInjectable(ComponentContext ic, Context context, Type type) {
        return type.equals(CustomFeature.class) ? this : null
    }

    @Override
    CustomFeature getValue(HttpContext c) {
        callCounter++
        return new CustomFeature();
    }
}
