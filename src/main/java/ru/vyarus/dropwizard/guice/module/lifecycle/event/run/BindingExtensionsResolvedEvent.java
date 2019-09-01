package ru.vyarus.dropwizard.guice.module.lifecycle.event.run;

import io.dropwizard.Configuration;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import ru.vyarus.dropwizard.guice.module.context.option.Options;
import ru.vyarus.dropwizard.guice.module.lifecycle.GuiceyLifecycle;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.RunPhaseEvent;
import ru.vyarus.dropwizard.guice.module.yaml.ConfigurationTree;

import java.util.List;

/**
 * Called when guice bindings analyzed and all extensions detected. Provides list of all recognized binding extensions
 * (including disabled). Called only if bindings analysis is enabled and at least one extension detected.
 * <p>
 * May be used for consultation only. Extension instances are not yet available (guice context is not created).
 *
 * @author Vyacheslav Rusakov
 * @since 01.09.2019
 */
public class BindingExtensionsResolvedEvent extends RunPhaseEvent {

    private final List<Class<?>> extensions;

    public BindingExtensionsResolvedEvent(final Options options,
                                          final Bootstrap bootstrap,
                                          final Configuration configuration,
                                          final ConfigurationTree configurationTree,
                                          final Environment environment,
                                          final List<Class<?>> extensions) {
        super(GuiceyLifecycle.BindingExtensionsResolved,
                options, bootstrap, configuration, configurationTree, environment);
        this.extensions = extensions;
    }

    /**
     * @return all registered manual extensions (including possibly disabled in the future)
     */
    public List<Class<?>> getExtensions() {
        return extensions;
    }
}
