package ru.vyarus.dropwizard.guice.module.jersey.support;

import com.google.inject.Injector;
import org.glassfish.hk2.api.Factory;

/**
 * Factory simply delegates object resolution to guice context. This allows to respect scopes.
 * But, more importantly, such "bridge" allows to bind guice type lazily.
 *
 * @param <T> injection type
 */
public class GuiceComponentFactory<T> implements Factory<T> {

    private final Injector injector;
    private final Class<T> type;

    public GuiceComponentFactory(final Injector injector, final Class<T> type) {
        this.injector = injector;
        this.type = type;
    }

    @Override
    public T provide() {
        return injector.getInstance(type);
    }

    @Override
    public void dispose(final T instance) {
        // do nothing
    }

    @Override
    public String toString() {
        return "GuiceComponentFactory for " + type;
    }
}
