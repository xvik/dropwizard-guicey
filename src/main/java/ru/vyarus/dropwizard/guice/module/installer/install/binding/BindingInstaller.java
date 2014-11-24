package ru.vyarus.dropwizard.guice.module.installer.install.binding;

import com.google.inject.Binder;

/**
 * Marker interface must be used together with {@code FeatureInstaller}.
 * Used for installers which requires custom bindings for extension.
 * Note: {@code binder.install(type)} is not called by default fo binding installer, assuming installer will
 * specify proper binding itself.
 * <p>If extension is annotated with {@code LazyBinding} boolean hint is passed and installer must decide
 * what to do with it (preferably support or fail, not ignore).</p>
 *
 * @author Vyacheslav Rusakov
 * @see ru.vyarus.dropwizard.guice.module.installer.feature.plugin.PluginInstaller
 * @since 09.10.2014
 */
public interface BindingInstaller {

    /**
     * Called to apply custom binding for installed feature.
     * All found features installed as {@code binder.install(type)} by default, so
     * implement it only if some custom binding required.
     *
     * @param binder guice binder
     * @param type   extension class
     * @param lazy   true if extension is annotated with {@code @LazyBinding}
     * @param <T>    plugin type (used just to define strict hierarchy and simplify binding)
     */
    <T> void install(Binder binder, Class<? extends T> type, boolean lazy);
}
