package ru.vyarus.dropwizard.guice.module.lifecycle.event.run;

import ru.vyarus.dropwizard.guice.module.lifecycle.GuiceyLifecycle;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.InjectorPhaseEvent;
import ru.vyarus.dropwizard.guice.module.lifecycle.internal.EventsContext;

import java.util.List;

/**
 * Called after all installers install related extensions.
 * Provides list of all used (enabled) extensions. Not called when no extensions installed.
 * <p>
 * Extension instance could be obtained manually from injector.
 *
 * @author Vyacheslav Rusakov
 * @since 19.04.2018
 */
public class ExtensionsInstalledEvent extends InjectorPhaseEvent {

    private final List<Class<?>> extensions;

    public ExtensionsInstalledEvent(final EventsContext context,
                                    final List<Class<?>> extensions) {
        super(GuiceyLifecycle.ExtensionsInstalled, context);
        this.extensions = extensions;
    }

    /**
     * @return list of all installed extensions
     */
    public List<Class<?>> getExtensions() {
        return extensions;
    }
}
