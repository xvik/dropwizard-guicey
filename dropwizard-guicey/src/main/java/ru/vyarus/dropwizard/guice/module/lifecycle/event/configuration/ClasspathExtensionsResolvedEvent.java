package ru.vyarus.dropwizard.guice.module.lifecycle.event.configuration;

import ru.vyarus.dropwizard.guice.module.lifecycle.GuiceyLifecycle;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.ConfigurationPhaseEvent;
import ru.vyarus.dropwizard.guice.module.lifecycle.internal.EventsContext;

import java.util.List;

/**
 * Called when classes from classpath scan analyzed and all extensions detected. Provides list of all extensions
 * detected in classpath (including disabled). Called only if classpath scan is enabled and at least one extension
 * detected.
 * <p>
 * May be used for consultation only. Extension instances are not yet available (guice context is not created).
 *
 * @author Vyacheslav Rusakov
 * @since 01.09.2019
 */
public class ClasspathExtensionsResolvedEvent extends ConfigurationPhaseEvent {

    private final List<Class<?>> extensions;

    /**
     * Create event.
     *
     * @param context    event context
     * @param extensions extensions
     */
    public ClasspathExtensionsResolvedEvent(final EventsContext context,
                                            final List<Class<?>> extensions) {
        super(GuiceyLifecycle.ClasspathExtensionsResolved, context);
        this.extensions = extensions;
    }

    /**
     * @return all detected classpath extensions (including possibly disabled in the future)
     */
    public List<Class<?>> getExtensions() {
        return extensions;
    }
}
