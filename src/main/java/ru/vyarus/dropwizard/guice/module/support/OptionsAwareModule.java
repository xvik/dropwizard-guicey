package ru.vyarus.dropwizard.guice.module.support;

import ru.vyarus.dropwizard.guice.module.context.option.Options;

/**
 * Guice module, registered in bundle, may implement this to be able to use options in module configuration method.
 *
 * @author Vyacheslav Rusakov
 * @since 25.04.2018
 */
public interface OptionsAwareModule {

    /**
     * Method will be called just before injector initialization.
     *
     * @param options options object
     */
    void setOptions(Options options);
}
