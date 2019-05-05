package ru.vyarus.dropwizard.guice.injector.jersey.util;

import javax.inject.Provider;
import java.util.function.Supplier;

/**
 * Used to return guice provider as supplier for jersey.
 *
 * @author Vyacheslav Rusakov
 * @since 25.04.2019
 */
public class SupplierProviderAdapter<T> implements Supplier<T> {

    private Provider<T> provider;

    public SupplierProviderAdapter(Provider<T> provider) {
        this.provider = provider;
    }

    @Override
    public T get() {
        return provider.get();
    }
}
