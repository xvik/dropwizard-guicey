package ru.vyarus.dropwizard.guice.module.lifecycle.event.configuration;

import io.dropwizard.setup.Bootstrap;
import ru.vyarus.dropwizard.guice.module.context.option.Options;
import ru.vyarus.dropwizard.guice.module.lifecycle.GuiceyLifecycle;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.ConfigurationPhaseEvent;

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

    public ClasspathExtensionsResolvedEvent(final Options options,
                                            final Bootstrap bootstrap,
                                            final List<Class<?>> extensions) {
        super(GuiceyLifecycle.ClasspathExtensionsResolved, options, bootstrap);
        this.extensions = extensions;
    }

    /**
     * @return all detected classpath extensions (including possibly disabled in the future)
     */
    public List<Class<?>> getExtensions() {
        return extensions;
    }
}
