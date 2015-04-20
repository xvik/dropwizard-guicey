package ru.vyarus.dropwizard.guice.module.jersey.support;

import com.google.inject.Injector;
import org.glassfish.hk2.api.Factory;

/**
 * Factory simply delegates object resolution to guice context. This allows to respect scopes.
 * But, more importantly, such "bridge" allows to bind guice type lazily.
 */
public class GuiceComponentFactory implements Factory {

    private final Injector injector;
    private final Class<?> type;

    public GuiceComponentFactory(final Injector injector, final Class<?> type) {
        this.injector = injector;
        this.type = type;
    }

    @Override
    public Object provide() {
        return injector.getInstance(type);
    }

    @Override
    public void dispose(final Object instance) {
        // do nothing
    }
}
