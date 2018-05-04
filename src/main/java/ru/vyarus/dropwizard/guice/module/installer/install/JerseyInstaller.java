package ru.vyarus.dropwizard.guice.module.installer.install;

import com.google.inject.Injector;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

/**
 * Marker interface for jersey extensions installer.Must be used together with {@code FeatureInstaller}.
 * Installer will be called in time of jersey start to properly register extensions in HK context.
 * Installer {@code report()} method will be called only after jersey start (so if, for example, environment
 * command is started, jersey specific extensions will not be logged).
 * <p>
 * Installer must support {@link ru.vyarus.dropwizard.guice.module.installer.feature.jersey.HK2Managed} and
 * {@link ru.vyarus.dropwizard.guice.module.installer.feature.jersey.GuiceManaged} annotations, which delegates
 * bean creation to HK2 or guice (as exception). Default target is declared by
 * {@link ru.vyarus.dropwizard.guice.module.installer.InstallersOptions#HkExtensionsManagedByGuice} option.
 * <p>
 * By default, jersey extensions should be registered in singleton scope, unless explicit scoping annotation
 * is present on bean. Forced singleton could be disabled with
 * {@link ru.vyarus.dropwizard.guice.module.installer.InstallersOptions#ForceSingletonForHkExtensions} option.
 * <p>
 * Use {@link ru.vyarus.dropwizard.guice.module.installer.util.JerseyBinding} to properly support annotation.
 *
 * @param <T> expected extension type (or Object when no super type (e.g. for annotated beans))
 * @author Vyacheslav Rusakov
 * @since 16.11.2014
 * @see ru.vyarus.dropwizard.guice.module.installer.feature.jersey.AbstractJerseyInstaller base class
 */
public interface JerseyInstaller<T> {

    /**
     * Called on jersey start to inject extensions into HK context.
     * Use {@link ru.vyarus.dropwizard.guice.module.installer.util.JerseyBinding} utility for proper types binding:
     * it provide utilities for various jersey extensions and use special "bridges" for registration to
     * respect guice scopes (most of the time we not register ready instance, but factory which delegates
     * creation to guice).
     *
     * @param binder   hk binder
     * @param injector guice injector
     * @param type     extension type to register
     * @see ru.vyarus.dropwizard.guice.module.installer.util.JerseyBinding
     * @see ru.vyarus.dropwizard.guice.module.installer.feature.jersey.HK2Managed
     */
    void install(AbstractBinder binder, Injector injector, Class<T> type);
}
