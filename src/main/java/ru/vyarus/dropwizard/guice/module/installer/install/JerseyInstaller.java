package ru.vyarus.dropwizard.guice.module.installer.install;

import org.glassfish.hk2.utilities.binding.AbstractBinder;

/**
 * Marker interface for jersey extensions installer.Must be used together with {@code FeatureInstaller}.
 * Installer will be called in time of jersey start to properly register extensions in HK context.
 * Installer {@code report()} method will be called only after jersey start (so if, for example, environment
 * command is started, jersey specific extensions will not be logged).
 * <p>Installer must support {@code HK2Managed} annotation, which delegates bean creation to HK2.
 * Use {@link ru.vyarus.dropwizard.guice.module.installer.util.JerseyBinding} to properly support annotation.</p>
 *
 * @author Vyacheslav Rusakov
 * @since 16.11.2014
 */
public interface JerseyInstaller {

    /**
     * Called on jersey start to inject extensions into HK context.
     * Use {@link ru.vyarus.dropwizard.guice.module.installer.util.JerseyBinding} utility for proper types binding:
     * it provide utilities for various jersey extensions and use special "bridges" for registration to
     * respect guice scopes (most of the time we not register ready instance, but factory which delegates
     * creation to guice).
     *
     * @param binder hk binder
     * @param type   extension type to register
     * @see ru.vyarus.dropwizard.guice.module.installer.util.JerseyBinding
     * @see ru.vyarus.dropwizard.guice.module.installer.feature.jersey.HK2Managed
     */
    void install(AbstractBinder binder, Class<?> type);
}
