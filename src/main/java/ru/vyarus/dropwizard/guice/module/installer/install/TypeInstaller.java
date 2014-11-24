package ru.vyarus.dropwizard.guice.module.installer.install;

import io.dropwizard.setup.Environment;

/**
 * Marker interface must be used together with {@code FeatureInstaller}.
 * Used for installers which requires just class to install extension.
 *
 * @param <T> expected extension type (or Object when no super type (e.g. for annotated beans))
 * @author Vyacheslav Rusakov
 * @see ru.vyarus.dropwizard.guice.module.installer.feature.jersey.ResourceInstaller
 * @since 10.10.2014
 */
public interface TypeInstaller<T> {

    /**
     * @param environment environment object
     * @param type        extension type
     */
    void install(Environment environment, Class<T> type);
}
