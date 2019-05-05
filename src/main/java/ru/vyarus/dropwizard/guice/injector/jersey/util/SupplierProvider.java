package ru.vyarus.dropwizard.guice.injector.jersey.util;

import com.google.inject.Inject;
import com.google.inject.Injector;

import javax.inject.Provider;
import java.util.function.Supplier;

/**
 * Used to register supplier class in guice context.
 *
 * @author Vyacheslav Rusakov
 * @since 25.04.2019
 */
public class SupplierProvider implements Provider {

    @Inject
    private Injector injector;

    private final Class<? extends Supplier> supplier;

    public SupplierProvider(Class<? extends Supplier> supplier) {
        this.supplier = supplier;
    }

    @Override
    public Object get() {
        return injector.getInstance(supplier).get();
    }
}
