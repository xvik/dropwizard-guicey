package ru.vyarus.dropwizard.guice.module.lifecycle.event.hk;

import com.google.inject.Injector;
import io.dropwizard.Configuration;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.glassfish.hk2.api.ServiceLocator;
import ru.vyarus.dropwizard.guice.module.context.option.Options;
import ru.vyarus.dropwizard.guice.module.lifecycle.GuiceyLifecycle;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.HkPhaseEvent;

/**
 * Hk context starting. At this point jersey is starting and jetty is only initializing. Guicey hk configuration
 * is not yer performed. Since that point hk {@link org.glassfish.hk2.api.ServiceLocator} is accessible.
 *
 * @author Vyacheslav Rusakov
 * @since 19.04.2018
 */
public class HkConfigurationEvent extends HkPhaseEvent {

    public HkConfigurationEvent(final Options options,
                                final Bootstrap bootstrap,
                                final Configuration configuration,
                                final Environment environment,
                                final Injector injector,
                                final ServiceLocator locator) {
        super(GuiceyLifecycle.HkConfiguration, options, bootstrap, configuration, environment, injector, locator);
    }
}
