package ru.vyarus.dropwizard.guice.module.context.option.internal;

import ru.vyarus.dropwizard.guice.module.context.option.Option;

/**
 * Holds used options (set or read) info.
 *
 * @param <T> option value type (actually not used due to enums,
 *            but stayed to indicate option and value type connection)
 * @author Vyacheslav Rusakov
 * @since 09.08.2016
 */
public final class OptionHolder<T> {
    private final Option<T> option;
    private boolean used;
    private boolean set;
    private T value;

    /**
     * Create holder.
     *
     * @param option option
     */
    public OptionHolder(final Option<T> option) {
        this.option = option;
        value = option.getDefaultValue();
    }

    /**
     * @return true if option value was read, false otherwise
     */
    public boolean isUsed() {
        return used;
    }

    /**
     * @return true if custom option value was set, false otherwise
     */
    public boolean isSet() {
        return set;
    }

    /**
     * @return option value (default of set)
     */
    public T getValue() {
        used = true;
        return value;
    }

    @Override
    public String toString() {
        return option + " = " + value;
    }

    /**
     * @param value new option value
     */
    public void setValue(final T value) {
        set = true;
        this.value = value;
    }
}
