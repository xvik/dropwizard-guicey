package ru.vyarus.dropwizard.guice.module.lifecycle.event.run;

import io.dropwizard.Configuration;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import ru.vyarus.dropwizard.guice.module.context.option.OptionsInfo;
import ru.vyarus.dropwizard.guice.module.lifecycle.GuiceyLifecycle;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.RunPhaseEvent;

import java.util.List;

/**
 * Called when all extensions detected (from classpath scan, if enabled). Provides list of all enabled
 * extension types.
 * <p>
 * All extensions are bound to guice context at that moment either directly (by default, all extensions are registered)
 * or with {@link ru.vyarus.dropwizard.guice.module.installer.install.binding.BindingInstaller}.
 * <p>
 * May be used for consultation only. Extension instances are not yet available (guice context is not created).
 *
 * @author Vyacheslav Rusakov
 * @since 19.04.2018
 */
public class ExtensionsResolvedEvent extends RunPhaseEvent {

    private final List<Class<?>> extensions;

    public ExtensionsResolvedEvent(final OptionsInfo options,
                                   final Bootstrap bootstrap,
                                   final Configuration configuration,
                                   final Environment environment,
                                   final List<Class<?>> extensions) {
        super(GuiceyLifecycle.ExtensionsResolved, options, bootstrap, configuration, environment);
        this.extensions = extensions;
    }

    /**
     * @return list of all enabled extensions or empty list if no extensions registered or all of them disabled
     */
    public List<Class<?>> getExtensions() {
        return extensions;
    }
}
