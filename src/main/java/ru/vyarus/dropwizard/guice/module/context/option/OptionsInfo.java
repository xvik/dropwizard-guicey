package ru.vyarus.dropwizard.guice.module.context.option;

import ru.vyarus.dropwizard.guice.module.context.option.internal.OptionHolder;
import ru.vyarus.dropwizard.guice.module.context.option.internal.OptionsSupport;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Provides access to used options and values. Contains only options either set
 * (in {@link ru.vyarus.dropwizard.guice.GuiceBundle}) or read (by guicey, bundles or guice services).
 * Instance bound to guice context and available for injection. Also may be accessed through
 * {@link ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo#getOptions()}.
 * <p>
 * NOTE: service just provides info of already used options (consultation only service - for reporting).
 * If option wasn't used then default value will not be returned (error will be thrown). Option value read will not
 * mark option as used. Do not use it for reading option value (for actual option usage): use {@link Options} instead.
 *
 * @author Vyacheslav Rusakov
 * @see Option for options details
 * @since 11.08.2016
 */
@SuppressWarnings("unchecked")
public final class OptionsInfo {

    private final OptionsSupport options;

    public OptionsInfo(final OptionsSupport support) {
        this.options = support;
    }

    /**
     * NOTE: as option could be accessed at any time by guice service, so more options could appear over time.
     * As a consequence, it can't be known for sure that option is not used, for example, just  after application start.
     *
     * @param <T> helper type to define option
     * @return set of all used options (either read or set)
     * @see #getOptionGroups() for option groups
     */
    public <T extends Enum & Option> Set<T> getOptions() {
        return options.getOptions();
    }

    /**
     * In contrast to {@link #getOptions()} returns only enum types of used options.
     * For example, {@link ru.vyarus.dropwizard.guice.GuiceyOptions} is enum type for core guicey options.
     *
     * @return set of used options enums
     */
    public Set<Class<Enum>> getOptionGroups() {
        return getOptions().stream().<Class<Enum>>map(Enum::getDeclaringClass).collect(Collectors.toSet());
    }

    /**
     * @param option option enum
     * @param <V>    value type
     * @param <T>    helper type to define option
     * @return current option value (default or defined)
     * @throws IllegalArgumentException if option was not used
     */
    public <V, T extends Enum & Option> V getValue(final T option) {
        final OptionHolder<V> holder = options.getHolder(option);
        return holder == null ? null : holder.getValue();
    }

    /**
     * Option usage tracked when option read by either
     * {@linkplain ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBootstrap#option(Enum) bundle} or
     * from {@linkplain Options#get(Enum) guice service}. Option may be read in any time of application life so
     * used value may change over time (even new options could appear for reporting).
     *
     * @param option option enum
     * @param <T>    helper type to define option
     * @return true if option was read, false if option set but never used
     * @throws IllegalArgumentException if option was not used
     */
    public <T extends Enum & Option> boolean isUsed(final T option) {
        final OptionHolder holder = options.getHolder(option);
        return holder != null && holder.isUsed();
    }

    /**
     * All options are set in application (before application start) and will not change after.
     *
     * @param option option enum
     * @param <T>    helper type to define option
     * @return true if option was manually defined, false when default value used
     * @throws IllegalArgumentException if option was not used
     */
    public <T extends Enum & Option> boolean isSet(final T option) {
        final OptionHolder holder = options.getHolder(option);
        return holder != null && holder.isSet();
    }

}
