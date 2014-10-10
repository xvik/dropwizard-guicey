package ru.vyarus.dropwizard.guice.module.installer.install;

import io.dropwizard.setup.Environment;

/**
 * Marker interface must be used together with {@code FeatureInstaller}.
 * Used for installers which require extension instance for installation.
 * Instance created using {@code injector.getInstance()}.
 * May be used to force guice bean instance creation (like eager singleton for development stage).
 *
 * @author Vyacheslav Rusakov
 * @since 10.10.2014
 * @see ru.vyarus.dropwizard.guice.module.installer.feature.ManagedInstaller
 * @param <T> expected extension type (or Object when no super type (e.g. for annotated beans))
 */
public interface InstanceInstaller<T> {

    /**
     * @param environment environment object
     * @param instance    extension instance
     */
    void install(Environment environment, T instance);
}
