package ru.vyarus.dropwizard.guice.module.lifecycle.event;

import io.dropwizard.Configuration;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import ru.vyarus.dropwizard.guice.module.context.option.Options;
import ru.vyarus.dropwizard.guice.module.lifecycle.GuiceyLifecycle;
import ru.vyarus.dropwizard.guice.module.yaml.ConfigurationTree;

/**
 * Base class for events, started after {@link ru.vyarus.dropwizard.guice.GuiceBundle#run(Configuration, Environment)}
 * phase. Most events will appear before ({@link io.dropwizard.Application#run(Configuration, Environment)}).
 *
 * @author Vyacheslav Rusakov
 * @since 19.04.2018
 */
public abstract class RunPhaseEvent extends GuiceyLifecycleEvent {

    private final Bootstrap bootstrap;
    private final Configuration configuration;
    private final ConfigurationTree configurationTree;
    private final Environment environment;

    public RunPhaseEvent(final GuiceyLifecycle type,
                         final Options options,
                         final Bootstrap bootstrap,
                         final Configuration configuration,
                         final ConfigurationTree configurationTree,
                         final Environment environment) {
        super(type, options);
        this.bootstrap = bootstrap;
        this.configuration = configuration;
        this.configurationTree = configurationTree;
        this.environment = environment;
    }

    /**
     * @return bootstrap object
     */
    public Bootstrap getBootstrap() {
        return bootstrap;
    }

    /**
     * @return configuration object
     */
    @SuppressWarnings("unchecked")
    public <T extends Configuration> T getConfiguration() {
        return (T) configuration;
    }

    /**
     * @return introspected configuration object
     */
    public ConfigurationTree getConfigurationTree() {
        return configurationTree;
    }

    /**
     * @return environment object
     */
    public Environment getEnvironment() {
        return environment;
    }
}
