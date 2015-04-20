package ru.vyarus.dropwizard.guice.module.jersey.support;

import com.google.inject.Injector;
import org.glassfish.hk2.api.Factory;

/**
 * Transitive factory helps "bridge" lazily real factories.
 * For example, if hk context is just starting and referenced guice bean depends on some hk bean,
 * we can't instantiate guice bean. This moves guice bean creation into hk init phase (when hk pre-init some factories)
 * or even further (first usage).
 */
public class LazyGuiceFactory implements Factory {

    private final Injector injector;
    private final Class<?> type;

    public LazyGuiceFactory(final Injector injector, final Class<?> type) {
        this.injector = injector;
        this.type = type;
    }

    @Override
    public Object provide() {
        return ((Factory) injector.getInstance(type)).provide();
    }

    @Override
    public void dispose(final Object instance) {
        // do nothing
    }
}
