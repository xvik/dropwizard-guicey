package ru.vyarus.dropwizard.guice.module.support;

import io.dropwizard.core.Configuration;

/**
 * Guice module, registered in bundle, may implement this to be able to use configuration object in module
 * configuration method.
 *
 * @param <T> configuration type
 * @author Vyacheslav Rusakov
 * @see DropwizardAwareModule
 * @since 31.08.2014
 */
public interface ConfigurationAwareModule<T extends Configuration> {

    /**
     * Method will be called just before injector initialization.
     *
     * @param configuration configuration object
     */
    void setConfiguration(T configuration);
}
