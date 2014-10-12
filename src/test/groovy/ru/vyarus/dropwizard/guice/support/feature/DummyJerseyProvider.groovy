package ru.vyarus.dropwizard.guice.support.feature

import com.sun.jersey.core.spi.component.ComponentContext
import com.sun.jersey.core.spi.component.ComponentScope
import com.sun.jersey.spi.inject.Injectable
import com.sun.jersey.spi.inject.InjectableProvider

import javax.ws.rs.core.Context
import javax.ws.rs.ext.Provider
import java.lang.reflect.Type

/**
 * Example from http://codahale.com/what-makes-jersey-interesting-injection-providers/.
 * @author Vyacheslav Rusakov 
 * @since 03.09.2014
 */
@Provider
class DummyJerseyProvider implements InjectableProvider<Context, Type> {

    @Override
    ComponentScope getScope() {
        return ComponentScope.PerRequest
    }

    @Override
    Injectable getInjectable(ComponentContext ic, Context context, Type type) {
        if (type.equals(Locale.class)) {
            return this;
        }

        return null;
    }
}
