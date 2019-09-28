package ru.vyarus.dropwizard.guice.module.installer.bundle.listener;

import com.google.inject.Injector;
import io.dropwizard.Configuration;
import io.dropwizard.setup.Environment;

/**
 * Called after guicey startup (after {@link ru.vyarus.dropwizard.guice.GuiceBundle#run(Configuration, Environment)}
 * method ) in order to perform manual configurations: like manual objects registration in dropwizard environment
 * (when dependent guice-managed objects required).
 *
 * @param <T> configuration type
 * @author Vyacheslav Rusakov
 * @since 28.09.2019
 */
@FunctionalInterface
public interface GuiceyStartupListener<T extends Configuration> {
    /**
     * Called after guicey startup to perform manual configurations using {@link Injector}.
     * <p>
     * Any thrown exception would shutdown startup.
     *
     * @param configuration configuration object
     * @param environment   environment object
     * @param injector      guice injector
     * @throws Exception in case of errors
     */
    void configure(T configuration, Environment environment, Injector injector) throws Exception;
}
