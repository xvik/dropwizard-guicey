package ru.vyarus.dropwizard.guice.module.installer;

import com.google.inject.Binder;

/**
 * Marker interface must be used together with {@code FeatureInstaller}.
 * Used for installers which requires custom bindings for extension.
 * Note: all found extension classes are always binded by type. Use this mechanism for additiona
 * custom bindings
 *
 * @author Vyacheslav Rusakov
 * @since 09.10.2014
 * @see ru.vyarus.dropwizard.guice.module.installer.feature.plugin.PluginInstaller
 */
public interface BindingInstaller {

    /**
     * Called to apply custom binding for installed feature.
     * All found features bind as binder.bind(type) by default, so
     * this method may be empty. Implement it only if some custom binding required.
     *
     * @param binder guice binder
     * @param type   extension class
     * @param <T> plugin type (used just to define strict hierarchy)
     */
    <T> void bind(Binder binder, Class<? extends T> type);
}
