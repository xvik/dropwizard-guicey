package ru.vyarus.dropwizard.guice.module.jersey.support;

import com.google.inject.Injector;
import org.glassfish.hk2.api.ServiceLocator;

import javax.inject.Provider;

/**
 * Lazy "bridge" used to register hk types in guice context. Guice context is created before hk,
 * so such lazy binding is the only way to register types properly.
 * <p>Provider used on stage when hk context is not started and guice context is gust starting,
 * so both injectors resolved lazily.</p>
 *
 * @param <T> injection type
 * @see ru.vyarus.dropwizard.guice.injector.lookup.InjectorProvider
 */
public class JerseyComponentProvider<T> implements Provider<T> {

    private final Provider<Injector> injector;
    private final Class<T> type;

    public JerseyComponentProvider(final Provider<Injector> injector, final Class<T> type) {
        this.injector = injector;
        this.type = type;
    }

    @Override
    public T get() {
        return injector.get().getInstance(ServiceLocator.class).getService(type);
    }
}
