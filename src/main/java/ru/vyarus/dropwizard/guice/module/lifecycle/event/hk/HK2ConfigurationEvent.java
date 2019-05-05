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

/**
 * Hk context starting. At this point jersey is starting and jetty is only initializing. Guicey hk configuration
 * is not yer performed. Since that point hk {@link InjectionManager} is accessible.
 *
 * @author Vyacheslav Rusakov
 * @since 19.04.2018
 */
public class HK2ConfigurationEvent extends HK2PhaseEvent {

    public HK2ConfigurationEvent(final Options options,
                                 final Bootstrap bootstrap,
                                 final Configuration configuration,
                                 final ConfigurationTree configurationTree,
                                 final Environment environment,
                                 final Injector injector,
                                 final InjectionManager locator) {
        super(GuiceyLifecycle.HK2Configuration, options, bootstrap,
                configuration, configurationTree, environment, injector, locator);
    }
}
