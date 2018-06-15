package ru.vyarus.dropwizard.guice.module.lifecycle.event;

import com.google.inject.Injector;
import io.dropwizard.Configuration;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.glassfish.hk2.api.ServiceLocator;
import ru.vyarus.dropwizard.guice.module.context.option.Options;
import ru.vyarus.dropwizard.guice.module.lifecycle.GuiceyLifecycle;
import ru.vyarus.dropwizard.guice.module.yaml.ConfigurationTree;

/**
 * Base class for events, started after HK context initialization start. Appears after jetty start, during
 * jersey initialization.
 *
 * @author Vyacheslav Rusakov
 * @since 19.04.2018
 */
public abstract class HK2PhaseEvent extends InjectorPhaseEvent {

    private final ServiceLocator locator;

    @SuppressWarnings("checkstyle:ParameterNumber")
    public HK2PhaseEvent(final GuiceyLifecycle type,
                         final Options options,
                         final Bootstrap bootstrap,
                         final Configuration configuration,
                         final ConfigurationTree configurationTree,
                         final Environment environment,
                         final Injector injector,
                         final ServiceLocator locator) {
        super(type, options, bootstrap, configuration, configurationTree, environment, injector);
        this.locator = locator;
    }

    /**
     * Note: all guicey events are happen before jersey application initialization finish and so locator can't be
     * used for extensions access, but it could be stored somewhere and used later (with help of jersey lifecycle
     * listener).
     *
     * @return root service locator
     */
    public ServiceLocator getLocator() {
        return locator;
    }
}
