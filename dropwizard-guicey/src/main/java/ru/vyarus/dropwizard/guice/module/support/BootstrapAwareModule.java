package ru.vyarus.dropwizard.guice.module.support;

import io.dropwizard.core.Configuration;
import io.dropwizard.core.setup.Bootstrap;

/**
 * Guice module, registered in bundle, may implement this to be able to use bootstrap object in module
 * configuration method.
 * <p>NOTE: setter will be called on run phase, so bootstrap object may be used only for reference
 * (too late for changes).</p>
 *
 * @param <T> configuration type
 * @author Vyacheslav Rusakov
 * @see DropwizardAwareModule
 * @since 31.08.2014
 */
public interface BootstrapAwareModule<T extends Configuration> {

    /**
     * Method will be called just before injector initialization.
     *
     * @param bootstrap bootstrap object
     */
    void setBootstrap(Bootstrap<T> bootstrap);
}
