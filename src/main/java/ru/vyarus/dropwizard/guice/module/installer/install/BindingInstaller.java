package ru.vyarus.dropwizard.guice.module.installer.install;

import com.google.inject.Binder;

/**
 * Marker interface must be used together with {@code FeatureInstaller}.
 * Used for installers which requires custom bindings for extension.
 * Note: all found extension classes are always binded by type. Use this mechanism for additional
 * custom bindings
 *
 * @author Vyacheslav Rusakov
 * @since 09.10.2014
 * @see ru.vyarus.dropwizard.guice.module.installer.feature.plugin.PluginInstaller
 */
public interface BindingInstaller {

    /**
     * Called to apply custom binding for installed feature.
     * All found features install as {@code binder.install(type)} by default, so
     * implement it only if some custom binding required.
     *
     * @param binder guice binder
     * @param type   extension class
     * @param <T> plugin type (used just to define strict hierarchy and simplify binding)
     */
    <T> void install(Binder binder, Class<? extends T> type);
}
