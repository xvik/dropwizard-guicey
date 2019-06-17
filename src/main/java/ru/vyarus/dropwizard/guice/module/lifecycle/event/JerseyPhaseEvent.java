package ru.vyarus.dropwizard.guice.module.lifecycle.event;

import com.google.inject.Injector;
import io.dropwizard.Configuration;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jersey.internal.inject.InjectionManager;
import ru.vyarus.dropwizard.guice.module.context.option.Options;
import ru.vyarus.dropwizard.guice.module.lifecycle.GuiceyLifecycle;
import ru.vyarus.dropwizard.guice.module.yaml.ConfigurationTree;

/**
 * Base class for events, started after jersey context initialization start. Appears after jetty start, during
 * jersey initialization.
 *
 * @author Vyacheslav Rusakov
 * @since 19.04.2018
 */
public abstract class JerseyPhaseEvent extends InjectorPhaseEvent {

    private final InjectionManager injectionManager;

    @SuppressWarnings("checkstyle:ParameterNumber")
    public JerseyPhaseEvent(final GuiceyLifecycle type,
                            final Options options,
                            final Bootstrap bootstrap,
                            final Configuration configuration,
                            final ConfigurationTree configurationTree,
                            final Environment environment,
                            final Injector injector,
                            final InjectionManager injectionManager) {
        super(type, options, bootstrap, configuration, configurationTree, environment, injector);
        this.injectionManager = injectionManager;
    }

    /**
     * Note: all guicey events are happen before jersey application initialization finish and so manager can't be
     * used for extensions access, but it could be stored somewhere and used later (with help of jersey lifecycle
     * listener).
     * <p>
     * Note: HK2 {@link ServiceLocator} could be obtained as bean from manager as
     * {@code getInjectionManager().getInstance(ServiceLocator.class)}.
     *
     * @return root service locator
     */
    public InjectionManager getInjectionManager() {
        return injectionManager;
    }
}
