package ru.vyarus.dropwizard.guice.module.jersey.support;

import com.google.inject.Injector;
import ru.vyarus.dropwizard.guice.debug.util.RenderUtils;

import java.util.function.Supplier;

/**
 * Factory simply delegates object resolution to guice context. This allows to respect scopes.
 * But, more importantly, such "bridge" allows to bind guice type lazily.
 *
 * @param <T> injection type
 */
public class GuiceComponentFactory<T> implements Supplier<T> {

    private final Injector injector;
    private final Class<T> type;

    /**
     * Create factory.
     *
     * @param injector injector
     * @param type     provided service type
     */
    public GuiceComponentFactory(final Injector injector, final Class<T> type) {
        this.injector = injector;
        this.type = type;
    }

    @Override
    public T get() {
        return injector.getInstance(type);
    }

    @Override
    public String toString() {
        return "GuiceComponentFactory for " + RenderUtils.getClassName(type);
    }
}
