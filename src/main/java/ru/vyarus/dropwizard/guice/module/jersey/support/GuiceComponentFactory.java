package ru.vyarus.dropwizard.guice.module.jersey.support;

import org.glassfish.hk2.api.Factory;
import ru.vyarus.dropwizard.guice.GuiceBundle;

/**
 * Factory simply delegates object resolution to guice context. This allows to respect scopes.
 * But, more importantly, such "bridge" allows to bing guice type lazily.
 */
public class GuiceComponentFactory implements Factory {

    private final Class<?> type;

    public GuiceComponentFactory(final Class<?> type) {
        this.type = type;
    }

    @Override
    public Object provide() {
        // obtaining injector statically, because there must be child injector, which is not created in time
        // of factory creation
        return GuiceBundle.getInjector().getInstance(type);
    }

    @Override
    public void dispose(final Object instance) {
        // do nothing
    }
}
