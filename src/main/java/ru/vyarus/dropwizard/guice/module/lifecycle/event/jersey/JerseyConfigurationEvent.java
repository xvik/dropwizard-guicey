package ru.vyarus.dropwizard.guice.module.lifecycle.event.jersey;

import com.google.inject.Injector;
import io.dropwizard.Configuration;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.glassfish.hk2.api.ServiceLocator;
import ru.vyarus.dropwizard.guice.module.context.option.Options;
import ru.vyarus.dropwizard.guice.module.lifecycle.GuiceyLifecycle;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.JerseyPhaseEvent;
import ru.vyarus.dropwizard.guice.module.yaml.ConfigurationTree;

/**
 * Jersey context starting. At this point jersey and jetty is only initializing. Guicey jersey configuration
 * is not yer performed. Since that point hk {@link org.glassfish.hk2.api.ServiceLocator} is accessible.
 *
 * @author Vyacheslav Rusakov
 * @since 19.04.2018
 */
public class JerseyConfigurationEvent extends JerseyPhaseEvent {

    public JerseyConfigurationEvent(final Options options,
                                    final Bootstrap bootstrap,
                                    final Configuration configuration,
                                    final ConfigurationTree configurationTree,
                                    final Environment environment,
                                    final Injector injector,
                                    final ServiceLocator locator) {
        super(GuiceyLifecycle.JerseyConfiguration, options, bootstrap,
                configuration, configurationTree, environment, injector, locator);
    }
}
