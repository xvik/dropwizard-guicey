package ru.vyarus.dropwizard.guice.module.lifecycle.event.run;

import ru.vyarus.dropwizard.guice.module.lifecycle.GuiceyLifecycle;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.RunPhaseEvent;
import ru.vyarus.dropwizard.guice.module.lifecycle.internal.EventsContext;

import java.util.List;

/**
 * Called when all extensions detected (from classpath scan and guice modules). Provides list of all enabled
 * and list of disabled extension types. Called even if no extensions configured to indicate configuration state.
 * <p>
 * May be used for consultation only. Extension instances are not yet available (guice context is not created).
 *
 * @author Vyacheslav Rusakov
 * @since 19.04.2018
 */
public class ExtensionsResolvedEvent extends RunPhaseEvent {

    private final List<Class<?>> extensions;
    private final List<Class<?>> disabled;

    public ExtensionsResolvedEvent(final EventsContext context,
                                   final List<Class<?>> extensions,
                                   final List<Class<?>> disabled) {
        super(GuiceyLifecycle.ExtensionsResolved, context);
        this.extensions = extensions;
        this.disabled = disabled;
    }

    /**
     * @return list of all enabled extensions or empty list if no extensions registered or all of them disabled
     */
    public List<Class<?>> getExtensions() {
        return extensions;
    }

    /**
     * @return list of disabled extensions or empty list
     */
    public List<Class<?>> getDisabled() {
        return disabled;
    }
}
