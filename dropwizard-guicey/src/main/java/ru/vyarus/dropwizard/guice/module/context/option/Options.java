package ru.vyarus.dropwizard.guice.module.context.option;

import ru.vyarus.dropwizard.guice.module.context.option.internal.OptionsSupport;

/**
 * Guice bean for accessing options from guice services. Bean usage is equivalent of
 * {@linkplain ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBootstrap#option(Enum) option method}
 * for guicey bundle.
 * <p>
 * Also used by installers to access options
 * (see {@link ru.vyarus.dropwizard.guice.module.installer.option.WithOptions}).
 * <p>
 * IMPORTANT: In contrast to {@link OptionsInfo} (which must be used for reporting only), this bean must be used
 * for real option value usage. Only this bean will correctly return default value for option not used before
 * (no set no gets).
 *
 * @author Vyacheslav Rusakov
 * @see Option for more info
 * @since 11.08.2016
 */
public final class Options {

    private final OptionsSupport optionsSupport;

    /**
     * Create options.
     *
     * @param optionsSupport options support
     */
    public Options(final OptionsSupport optionsSupport) {
        this.optionsSupport = optionsSupport;
    }

    /**
     * Read option value. Options could be set only in application root
     * {@link ru.vyarus.dropwizard.guice.GuiceBundle.Builder#option(Enum, Object)}.
     * If value wasn't set there then default value will be returned. Null may return only if it was default value
     * and no new value were assigned.
     * <p>
     * Option access is tracked as option usage
     * (see {@link ru.vyarus.dropwizard.guice.module.context.option.OptionsInfo} for options info).
     *
     * @param option option enum
     * @param <V>    option value type
     * @param <T>    helper type to define option
     * @return assigned option value or default value
     * @see Option for more info about options
     * @see ru.vyarus.dropwizard.guice.GuiceyOptions for options example
     */
    @SuppressWarnings("unchecked")
    public <V, T extends Enum & Option> V get(final T option) {
        return (V) optionsSupport.get(option);
    }
}
