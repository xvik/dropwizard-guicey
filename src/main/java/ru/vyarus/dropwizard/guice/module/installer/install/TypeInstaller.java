package ru.vyarus.dropwizard.guice.module.installer.install;

import io.dropwizard.core.setup.Environment;

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
     * Full {@link com.google.inject.Injector} could be obtained with
     * {@link ru.vyarus.dropwizard.guice.injector.lookup.InjectorLookup#getInjector(Environment)}.
     * <p>
     * Shared state could be obtained with
     * {@link ru.vyarus.dropwizard.guice.module.context.SharedConfigurationState#get(Environment)}
     *
     * @param environment environment object
     * @param type        extension type
     */
    void install(Environment environment, Class<T> type);
}
