package ru.vyarus.dropwizard.guice.test.client.util;

import java.util.function.Supplier;

/**
 * Value wrapper with a registration source (for debug output).
 *
 * @param <V> value type
 * @author Vyacheslav Rusakov
 * @since 03.10.2025
 */
public class SourceAwareValue<V> implements Supplier<V> {
    private final Supplier<V> supplier;
    private final String source;

    /**
     * Create source value.
     *
     * @param supplier value supplier
     * @param source registration source (for debug output)
     */
    public SourceAwareValue(final Supplier<V> supplier, final String source) {
        this.supplier = supplier;
        this.source = source;
    }

    @Override
    public V get() {
        return supplier.get();
    }

    /**
     * @return registration source (for debug output)
     */
    public String getSource() {
        return source;
    }

    @Override
    public String toString() {
        return "Value from " + source;
    }
}
