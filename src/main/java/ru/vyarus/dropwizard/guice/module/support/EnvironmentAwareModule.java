package ru.vyarus.dropwizard.guice.module.support;

import io.dropwizard.setup.Environment;

/**
 * Guice module, registered in bundle, may implement this to be able to use environment object in module
 * configuration method.
 *
 * @author Vyacheslav Rusakov
 * @since 31.08.2014
 */
public interface EnvironmentAwareModule {

    /**
     * Method will be called just before injector initialization.
     *
     * @param environment environment object
     */
    void setEnvironment(Environment environment);
}
