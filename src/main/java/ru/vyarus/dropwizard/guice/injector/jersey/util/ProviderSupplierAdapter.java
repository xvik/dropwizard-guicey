package ru.vyarus.dropwizard.guice.injector.jersey.util;

import javax.inject.Provider;
import java.util.function.Supplier;

/**
 * Used to register jersey supplier instance as provider in guice.
 *
 * @author Vyacheslav Rusakov
 * @since 25.04.2019
 */
public class ProviderSupplierAdapter<T> implements Provider<T> {
    private Supplier<T> supplier;

    public ProviderSupplierAdapter(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    @Override
    public T get() {
        return supplier.get();
    }
}
