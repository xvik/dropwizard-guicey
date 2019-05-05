package ru.vyarus.dropwizard.guice.module.lifecycle.event.hk;

import com.google.inject.Injector;
import io.dropwizard.Configuration;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.glassfish.jersey.internal.inject.InjectionManager;
import ru.vyarus.dropwizard.guice.module.context.option.Options;
import ru.vyarus.dropwizard.guice.module.lifecycle.GuiceyLifecycle;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.HK2PhaseEvent;
import ru.vyarus.dropwizard.guice.module.yaml.ConfigurationTree;

import java.util.List;

/**
 * Called after all {@link ru.vyarus.dropwizard.guice.module.installer.install.JerseyInstaller} installers install
 * related extensions and only when at least one extension was installed. Provides list of all used (enabled)
 * extensions.
 * <p>
 * At this point hk is not completely started and so hk managed extensions
 * ({@link ru.vyarus.dropwizard.guice.module.installer.feature.jersey.HK2Managed}) couldn't be obtained yet
 * (even though you have access to root service locator). But extensions managed by guice could be obtained
 * from guice context.
 * <p>
 * To listen hk lifecycle further use jersey events (like in
 * {@link ru.vyarus.dropwizard.guice.module.lifecycle.debug.DebugGuiceyLifecycle}).
 *
 * @author Vyacheslav Rusakov
 * @since 19.04.2018
 */
public class HK2ExtensionsInstalledEvent extends HK2PhaseEvent {

    private final List<Class<?>> extensions;

    @SuppressWarnings("checkstyle:ParameterNumber")
    public HK2ExtensionsInstalledEvent(final Options options,
                                       final Bootstrap bootstrap,
                                       final Configuration configuration,
                                       final ConfigurationTree configurationTree,
                                       final Environment environment,
                                       final Injector injector,
                                       final InjectionManager locator,
                                       final List<Class<?>> extensions) {
        super(GuiceyLifecycle.HK2ExtensionsInstalled, options, bootstrap,
                configuration, configurationTree, environment, injector, locator);
        this.extensions = extensions;
    }

    /**
     * @return list of all installed extensions
     */
    public List<Class<?>> getExtensions() {
        return extensions;
    }
}
