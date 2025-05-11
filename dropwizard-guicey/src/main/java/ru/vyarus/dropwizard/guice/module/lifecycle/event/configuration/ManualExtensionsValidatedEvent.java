package ru.vyarus.dropwizard.guice.module.lifecycle.event.configuration;

import ru.vyarus.dropwizard.guice.module.lifecycle.GuiceyLifecycle;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.RunPhaseEvent;
import ru.vyarus.dropwizard.guice.module.lifecycle.internal.EventsContext;

import java.util.List;

/**
 * Called when all manually registered extension classes are recognized by installers (validated). But only
 * extensions, known to be enabled at that time are actually validated (this way it is possible to exclude
 * extensions for non existing installers). Provides list of all extensions registered manually (including disabled)
 * and actually validated extensions (known to be enabled at the time of validation).
 * Called only if at least one manual extension registered.
 * <p>
 * May be used for consultation only. Extension instances are not yet available (guice context is not created).
 *
 * @author Vyacheslav Rusakov
 * @since 01.09.2019
 */
public class ManualExtensionsValidatedEvent extends RunPhaseEvent {

    private final List<Class<?>> extensions;
    private final List<Class<?>> validated;

    /**
     * Create event.
     *
     * @param context    event context
     * @param extensions all extensions
     * @param validated  manual extensions
     */
    public ManualExtensionsValidatedEvent(final EventsContext context,
                                          final List<Class<?>> extensions,
                                          final List<Class<?>> validated) {
        super(GuiceyLifecycle.ManualExtensionsValidated, context);
        this.extensions = extensions;
        this.validated = validated;
    }

    /**
     * @return all detected binding extensions (including possibly disabled in the future)
     */
    public List<Class<?>> getExtensions() {
        return extensions;
    }

    /**
     * Note that some of these extensions may still be disabled in the run phase.
     *
     * @return actually validated extensions (known to be enabled at the time of validation)
     */
    public List<Class<?>> getValidated() {
        return validated;
    }
}
