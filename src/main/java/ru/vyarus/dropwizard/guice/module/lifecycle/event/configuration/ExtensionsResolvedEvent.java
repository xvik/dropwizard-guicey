package ru.vyarus.dropwizard.guice.module.lifecycle.event.configuration;

import io.dropwizard.setup.Bootstrap;
import ru.vyarus.dropwizard.guice.module.context.option.Options;
import ru.vyarus.dropwizard.guice.module.lifecycle.GuiceyLifecycle;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.ConfigurationPhaseEvent;

import java.util.List;

/**
 * Called when all extensions detected (from classpath scan, if enabled). Provides list of all enabled
 * and list of disabled extension types. Be careful: this is a final list of extensions, but some extensions
 * may be still disabled in run phase.
 * <p>
 * All extensions are bound to guice context at that moment either directly (by default, all extensions are registered)
 * or with {@link ru.vyarus.dropwizard.guice.module.installer.install.binding.BindingInstaller}.
 * <p>
 * May be used for consultation only. Extension instances are not yet available (guice context is not created).
 *
 * @author Vyacheslav Rusakov
 * @since 19.04.2018
 */
public class ExtensionsResolvedEvent extends ConfigurationPhaseEvent {

    private final List<Class<?>> extensions;
    private final List<Class<?>> disabled;

    public ExtensionsResolvedEvent(final Options options,
                                   final Bootstrap bootstrap,
                                   final List<Class<?>> extensions,
                                   final List<Class<?>> disabled) {
        super(GuiceyLifecycle.ExtensionsResolved, options, bootstrap);
        this.extensions = extensions;
        this.disabled = disabled;
    }

    /**
     * WARNING: some extensions may be still disabled later during run phase!
     *
     * @return list of all enabled extensions or empty list if no extensions registered or all of them disabled
     */
    public List<Class<?>> getExtensions() {
        return extensions;
    }

    /**
     * WARNING: some extensions may be still disabled later during run phase!
     * 
     * @return list of disabled extensions or empty list
     */
    public List<Class<?>> getDisabled() {
        return disabled;
    }
}
