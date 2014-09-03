package ru.vyarus.dropwizard.guice.module.autoconfig.feature;

import io.dropwizard.setup.Environment;

/**
 * Installer serve two purposes: find extension on classpath and properly install it
 * (in dropwizard or somewhere else). Each installer should work with single feature.
 * <p>Installers are not guice beans: they are instantiated during guice context start and used to register
 * additional beans in guice context.</p>
 * <p>After context start installer is called to properly install class (register health check, resource, etc.).</p>
 *
 * @param <T> expected extension type (or Object when no super type (e.g. for annotated beans))
 */
public interface FeatureInstaller<T> {

    /**
     * NOTE: consider using {@code ru.vyarus.dropwizard.guice.module.autoconfig.util.FeatureUtils} to simplify checks
     * (for example, types most likely must be checks to be not abstract).
     * <br/>
     * When type accepted by any extension it's registered in guice module.
     *
     * @param type type to check
     * @return true if extension recognized, false otherwise
     */
    boolean matches(final Class<?> type);

    /**
     * Every found extension (during classpath scan) is instantiated with guice and pass here for
     * further registration.
     *
     * @param environment environment object
     * @param instance    extension instance
     */
    void install(final Environment environment, final T instance);
}
