package ru.vyarus.dropwizard.guice.module.jersey.support;

import com.google.inject.Injector;
import org.glassfish.hk2.api.ProxyCtl;
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
    @SuppressWarnings("unchecked")
    public T get() {
        // hk2 by default proxy instances to delay actual instance creation, which could harm guice scopes logic
        // for example: if guice request scope transfer used ServletScopes.transferRequest and we try to obtain
        // it will try to use proxy instance in separate thread which will perform hk2 checks for request scope
        // and fail. Instead, we always resolve actual instance and let guice properly control scoping
        final T res = injector.get().getInstance(ServiceLocator.class).getService(type);
        return res instanceof ProxyCtl ? (T) ((ProxyCtl) res).__make() : res;
    }

    @Override
    public String toString() {
        return "JerseyComponentProvider for " + type;
    }
}
