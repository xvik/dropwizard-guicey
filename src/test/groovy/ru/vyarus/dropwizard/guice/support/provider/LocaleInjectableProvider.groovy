package ru.vyarus.dropwizard.guice.support.provider

import com.sun.jersey.api.core.HttpContext
import com.sun.jersey.core.spi.component.ComponentContext
import com.sun.jersey.core.spi.component.ComponentScope
import com.sun.jersey.server.impl.inject.AbstractHttpContextInjectable
import com.sun.jersey.spi.inject.Injectable
import com.sun.jersey.spi.inject.InjectableProvider

import javax.ws.rs.core.Context
import java.lang.reflect.Type

/**
 * Sample injectable provider which injects Locale instance from request.
 *
 * @author Vyacheslav Rusakov 
 * @since 09.10.2014
 */
class LocaleInjectableProvider extends AbstractHttpContextInjectable<Locale>
        implements InjectableProvider<Context, Type> {
    static int creationCounter = 0
    static int callCounter = 0

    LocaleInjectableProvider() {
        creationCounter++
    }

    @Override
    ComponentScope getScope() {
        //ignored
        return ComponentScope.PerRequest
    }

    @Override
    Injectable getInjectable(ComponentContext ic, Context context, Type type) {
        return type.equals(Locale.class) ? this : null
    }

    @Override
    Locale getValue(HttpContext c) {
        callCounter++
        final List<Locale> locales = c.getRequest().getAcceptableLanguages();
        return locales.isEmpty() ? Locale.US : locales.get(0);
    }
}
