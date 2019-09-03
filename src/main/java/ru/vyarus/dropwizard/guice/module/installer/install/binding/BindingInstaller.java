package ru.vyarus.dropwizard.guice.module.installer.install.binding;

import com.google.inject.Binder;
import com.google.inject.Binding;

/**
 * Marker interface must be used together with {@code FeatureInstaller}.
 * Used for installers which requires custom bindings for extension.
 * Note: {@code binder.bind(type)} is not called by default for binding installer, assuming installer will
 * specify proper binding itself.
 * <p>
 * <ul>
 * <li>{@link #bindExtension(Binder, Class, boolean)} called for extensions not manually bound in guice</li>
 * <li>{@link #checkBinding(Binder, Class, Binding)} called for extensions, resolved from manual guice binding</li>
 * <li>{@link #installBinding(Binder, Class)} is always called after one of install methods above in order to
 * apply registrations common for both registration types. Ideal for reporting.</li>
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
    void bindExtension(Binder binder, Class<?> type, boolean lazy);

    /**
     * Called for extensions, resolved from guice bindings (in user modules). May be used to validate binding
     * or do some additional bindings with existing binding.
     * <p>
     * {@link LazyBinding} flag is not used here because guicey will automatically throw an error if existing binding
     * is annotated as lazy binding (which don't makes any sense).
     * <p>
     * WARNING: may be called multiple times due to enabled bindings reporting! Be sure to produce correct logs.
     *
     * @param binder        guice binder
     * @param type          extension class
     * @param manualBinding binding declaration from guice module
     * @param <T>           extension type, used to connect extension class with binding
     */
    <T> void checkBinding(Binder binder, Class<T> type, Binding<T> manualBinding);

    /**
     * Called after execution of one of above methods. May be useful to perform common actions for both cases
     * or simply for reporting.
     * <p>
     * WARNING: may be called multiple times due to enabled bindings reporting! Be sure to produce correct logs.
     *
     * @param binder guice binder
     * @param type   extension class
     */
    void installBinding(Binder binder, Class<?> type);
}
