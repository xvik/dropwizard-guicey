package ru.vyarus.dropwizard.guice.module.lifecycle.event.run;

import com.google.inject.Injector;
import io.dropwizard.Configuration;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import ru.vyarus.dropwizard.guice.module.context.option.Options;
import ru.vyarus.dropwizard.guice.module.lifecycle.GuiceyLifecycle;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.InjectorPhaseEvent;
import ru.vyarus.dropwizard.guice.module.yaml.ConfigurationTree;

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

    public ExtensionsInstalledEvent(final Options options,
                                    final Bootstrap bootstrap,
                                    final Configuration configuration,
                                    final ConfigurationTree configurationTree,
                                    final Environment environment,
                                    final Injector injector,
                                    final List<Class<?>> extensions) {
        super(GuiceyLifecycle.ExtensionsInstalled, options, bootstrap,
                configuration, configurationTree, environment, injector);
        this.extensions = extensions;
    }

    /**
     * @return list of all installed extensions
     */
    public List<Class<?>> getExtensions() {
        return extensions;
    }
}
