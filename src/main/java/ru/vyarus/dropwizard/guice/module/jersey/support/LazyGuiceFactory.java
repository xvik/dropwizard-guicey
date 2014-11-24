package ru.vyarus.dropwizard.guice.module.jersey.support;

import org.glassfish.hk2.api.Factory;
import ru.vyarus.dropwizard.guice.GuiceBundle;

/**
 * Transitive factory helps "bridge" lazily real factories.
 */
public class LazyGuiceFactory implements Factory {
    private final Class<?> type;

    public LazyGuiceFactory(final Class<?> type) {
        this.type = type;
    }

    @Override
    public Object provide() {
        return ((Factory) GuiceBundle.getInjector().getInstance(type)).provide();
    }

    @Override
    public void dispose(final Object instance) {
        // do nothing
    }
}
