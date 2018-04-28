package ru.vyarus.dropwizard.guice.module.context.option;

/**
 * Options used for finer configuration. In contrast to dropwizard configuration file, which is user specific,
 * options are set during development and represent developer decisions. Options must be grouped with enum
 * (for example, see {@link ru.vyarus.dropwizard.guice.GuiceyOptions}. Usage within enums does not allow
 * using generics for option values checking, so option type set explicitly and used internally to validate
 * option type on option value set.
 * <p>
 * Options could be set only on application (root) level using
 * {@link ru.vyarus.dropwizard.guice.GuiceBundle.Builder#option(Enum, Object)} method, so all options are always set
 * before bundles processing.
 * <p>
 * Option may provide default value (useful for most cases, especially boolean options). Option may not be
 * set to null (protected by implementation).
 * <p>
 * Option may be accessed in bundle using
 * {@link ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBootstrap#option(Enum)} method or using
 * {@link Options} guice bean. Installers could access options using
 * {@link ru.vyarus.dropwizard.guice.module.installer.option.WithOptions} interface.
 * <p>
 * Option reads and writes are tracked in order to detect useless and default value options. All tracked information
 * is available through {@link OptionsInfo} guice bean.
 *
 * @param <T> option type (actually not used in the implementation but showing here connection between type
 *            declaration and option value)
 * @author Vyacheslav Rusakov
 * @since 09.08.2016
 */
public interface Option<T> {

    /**
     * @return option type
     */
    Class<T> getType();

    /**
     * @return option default value or null
     */
    T getDefaultValue();
}
