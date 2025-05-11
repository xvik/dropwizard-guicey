package ru.vyarus.dropwizard.guice.module.jersey.support;

import com.google.inject.Injector;
import ru.vyarus.dropwizard.guice.debug.util.RenderUtils;

import java.util.function.Supplier;

/**
 * Transitive factory helps "bridge" lazily real factories.
 * For example, if HK2 context is just starting and referenced guice bean depends on some HK2 bean,
 * we can't instantiate guice bean. This moves guice bean creation into HK2 init phase (when HK2 pre-init some
 * factories) or even further (first usage).
 *
 * @param <T> injection type
 */
public class LazyGuiceFactory<T> implements Supplier<T> {

    private final Injector injector;
    private final Class<Supplier<T>> type;

    /**
     * Create factory.
     *
     * @param injector injector
     * @param type     original factory
     */
    public LazyGuiceFactory(final Injector injector, final Class<Supplier<T>> type) {
        this.injector = injector;
        this.type = type;
    }

    @Override
    public T get() {
        return injector.getInstance(type).get();
    }

    @Override
    public String toString() {
        return "LazyGuiceFactory for " + RenderUtils.getClassName(type);
    }
}
