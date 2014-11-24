package ru.vyarus.dropwizard.guice.module.jersey.support;

import org.glassfish.hk2.api.ServiceLocator;
import ru.vyarus.dropwizard.guice.GuiceBundle;

import javax.inject.Provider;

/**
 * Lazy "bridge" used to register hk types in guice context. Guice context is created before hk,
 * so such lazy binding is the only way to register types properly.
 *
 * @param <T> injection type
 */
public class JerseyComponentProvider<T> implements Provider<T> {

    private final Class<T> type;

    public JerseyComponentProvider(final Class<T> type) {
        this.type = type;
    }

    @Override
    public T get() {
        return GuiceBundle.getInjector().getInstance(ServiceLocator.class).getService(type);
    }
}
