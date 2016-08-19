package ru.vyarus.dropwizard.guice.module.installer.option;

import ru.vyarus.dropwizard.guice.module.context.option.Option;
import ru.vyarus.dropwizard.guice.module.context.option.Options;

/**
 * Base class implementing options support for installers. May be used instead of directly implementing
 * {@link WithOptions}.
 *
 * @author Vyacheslav Rusakov
 * @since 20.08.2016
 */
public abstract class InstallerOptionsSupport implements WithOptions {

    private Options options;

    @Override
    public void setOptions(final Options options) {
        this.options = options;
    }

    /**
     * @param option option enum
     * @param <V>    option value type
     * @param <T>    helper type to define option
     * @return assigned option value or default value
     * @see Option for more info about options
     * @see Options#get(java.lang.Enum) for details
     * @see ru.vyarus.dropwizard.guice.GuiceyOptions for options example
     */
    protected <V, T extends Enum & Option> V option(final T option) {
        return options.get(option);
    }
}
