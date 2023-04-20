package ru.vyarus.guicey.jdbi3.dbi;

import io.dropwizard.Configuration;
import io.dropwizard.setup.Environment;

/**
 * Helper for implementing lazy initialization. Useful in initialization part where bundles are configured.
 * For example, to construct some dropwizard integration object and use it in guice integrations later.
 *
 * @param <T> provided object type
 * @param <C> configuration type
 * @author Vyacheslav Rusakov
 * @since 31.08.2018
 */
@FunctionalInterface
public interface ConfigAwareProvider<T, C extends Configuration> {

    /**
     * Called to provide required object.
     *
     * @param configuration configuration instance
     * @param environment   environment instance
     * @return object instance
     */
    T get(C configuration, Environment environment);
}
