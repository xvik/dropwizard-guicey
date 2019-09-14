package ru.vyarus.dropwizard.guice.module.context.option;

import ru.vyarus.dropwizard.guice.module.context.option.internal.OptionHolder;
import ru.vyarus.dropwizard.guice.module.context.option.internal.OptionsSupport;

import java.util.Comparator;
import java.util.List;
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
 * <p>
 * Most methods accept simple Enum instead of Enum &amp; Option to simplify generic usage: if not allowed enum
 * value will be used methods will throw exceptions. {@link #knowsOption(Enum)} method may be used to
 * safely check enum value before calling actual data methods.
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
     * @return list of used option enums sorted by name
     */
    public List<Class<Enum>> getOptionGroups() {
        // java 11 compilation workaround (for cannot infer type error)
        return (List<Class<Enum>>) (List) getOptions().stream().map(Enum::getDeclaringClass).distinct()
                .sorted(Comparator.comparing(Class::getSimpleName))
                .collect(Collectors.toList());
    }

    /**
     * @param option option enum
     * @param <V>    value type
     * @return current option value (default or defined)
     * @throws IllegalArgumentException if option was not used
     */
    public <V> V getValue(final Enum option) {
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
     * @return true if option was read, false if option set but never used
     * @throws IllegalArgumentException if option was not used
     */
    public boolean isUsed(final Enum option) {
        final OptionHolder holder = options.getHolder(option);
        return holder != null && holder.isUsed();
    }

    /**
     * All options are set in application (before application start) and will not change after.
     *
     * @param option option enum
     * @return true if option was manually defined, false when default value used
     * @throws IllegalArgumentException if option was not used
     */
    public boolean isSet(final Enum option) {
        final OptionHolder holder = options.getHolder(option);
        return holder != null && holder.isSet();
    }

    /**
     * Use this method to check if option is available for reporting (option details methods will fail with
     * exception if not known option will be provided).
     *
     * @param option option enum
     * @return true if option registered (was used or set), false otherwise
     */
    public boolean knowsOption(final Enum option) {
        return options.containsOption(option);
    }
}
