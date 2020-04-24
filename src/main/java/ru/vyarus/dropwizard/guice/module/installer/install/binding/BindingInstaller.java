package ru.vyarus.dropwizard.guice.module.installer.install.binding;

import com.google.inject.Binder;
import com.google.inject.Binding;
import com.google.inject.Stage;

/**
 * Marker interface must be used together with {@code FeatureInstaller}.
 * Used for installers which requires custom bindings for extension.
 * Note: {@code binder.bind(type)} is not called by default for binding installer, assuming installer will
 * specify proper binding itself.
 * <ul>
 * <li>{@link #bind(Binder, Class, boolean)} called for extensions not manually bound in guice</li>
 * <li>{@link #manualBinding(Binder, Class, Binding)} called for extensions, resolved from manual guice binding</li>
 * <li>{@link #extensionBound(Stage, Class)} (Binder, Class)} is always called after one of install methods above in
 * order to apply actions common for both registration types (usually reporting).</li>
 * </ul>
 * <p>
 * Install methods may be also used to restrict extension declaration only as class or bindings.
 * <p>
 * If extension is annotated with {@code LazyBinding} boolean hint is passed and installer must decide
 * what to do with it (preferably support or fail, not ignore). Bindings extensions (resolved from guice modules)
 * may not be declared as lazy (this will be detected automatically).
 * <p>
 * Installer may be called multiple times if guice bindings or aop report is enabled. If required, report execution
 * may be detected by used stage: {@code binder.currentStage() == Stage.TOOL}.
 *
 * @author Vyacheslav Rusakov
 * @see ru.vyarus.dropwizard.guice.module.installer.feature.plugin.PluginInstaller
 * @since 09.10.2014
 */
public interface BindingInstaller {

    /**
     * Called to apply custom binding for installed feature.
     * By default, all found extensions are installed as {@code binder.bind(type)}. This method will be called
     * instead of default registration.
     * <p>
     * Called only for non binding extensions (extensions that are not already bound in guice module.
     * <p>
     * WARNING: may be called multiple times due to enabled bindings reporting! Be sure to produce correct logs.
     *
     * @param binder guice binder
     * @param type   extension class
     * @param lazy   true if extension is annotated with {@link LazyBinding}
     */
    void bind(Binder binder, Class<?> type, boolean lazy);

    /**
     * Called for extensions, resolved from guice bindings (in user modules). May be used to validate binding
     * or do some additional bindings with existing binding. Binding is detected primarily by main key
     * {@link Binding#getKey()}, but linked bindings ({@link com.google.inject.spi.LinkedKeyBinding}) are
     * also checked for target key ({@link com.google.inject.spi.LinkedKeyBinding#getLinkedKey()}).
     * <p>
     * {@link LazyBinding} flag is not used here because guicey will automatically throw an error if existing binding
     * is annotated as lazy binding (which don't makes any sense).
     * <p>
     * WARNING: may be called multiple times due to enabled bindings reporting! Be sure to produce correct logs.
     *
     * @param binder  guice binder
     * @param type    extension class
     * @param binding binding declaration from guice module
     * @param <T>     extension type, used to connect extension class with binding
     * @see ru.vyarus.dropwizard.guice.module.installer.util.BindingUtils#getDeclarationSource(Binding)
     * for errors reporting
     */
    default <T> void manualBinding(Binder binder, Class<T> type, Binding<T> binding) {
        // no actions by default
    }

    /**
     * Called after execution of one of above methods. Useful for reporting. {@link Binder} is not provided
     * here to avoid confusion with {@link #bind(Binder, Class, boolean)}.
     * <p>
     * WARNING: may be called multiple times due to enabled bindings reporting! Be sure to produce correct logs.
     *
     * @param stage stage (useful for differentiation between normal run ({@link Stage#PRODUCTION}) and report
     *              rendering ({@link Stage#TOOL})
     * @param type  extension class
     */
    default void extensionBound(Stage stage, Class<?> type) {
        // no actions by default
    }
}
