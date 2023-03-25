package ru.vyarus.dropwizard.guice.module.lifecycle.event;

import io.dropwizard.core.Configuration;
import io.dropwizard.core.setup.Environment;
import ru.vyarus.dropwizard.guice.debug.report.yaml.BindingsConfig;
import ru.vyarus.dropwizard.guice.debug.report.yaml.ConfigBindingsRenderer;
import ru.vyarus.dropwizard.guice.module.lifecycle.GuiceyLifecycle;
import ru.vyarus.dropwizard.guice.module.lifecycle.internal.EventsContext;
import ru.vyarus.dropwizard.guice.module.yaml.ConfigurationTree;

/**
 * Base class for events, started after {@link ru.vyarus.dropwizard.guice.GuiceBundle#run(Configuration, Environment)}
 * phase. Most events will appear before ({@link io.dropwizard.core.Application#run(Configuration, Environment)}).
 *
 * @author Vyacheslav Rusakov
 * @since 19.04.2018
 */
public abstract class RunPhaseEvent extends ConfigurationPhaseEvent {

    private final Configuration configuration;
    private final ConfigurationTree configurationTree;
    private final Environment environment;

    public RunPhaseEvent(final GuiceyLifecycle type,
                         final EventsContext context) {
        super(type, context);
        this.configuration = context.getConfiguration();
        this.configurationTree = context.getConfigurationTree();
        this.environment = context.getEnvironment();
    }

    /**
     * @param <T> configuration type
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

    /**
     * Configuration report shows configuration binding paths.
     *
     * @param config configuration bindings report configuration
     * @return rendered report
     * @see InjectorPhaseEvent#getReportRenderer() for configuration reports (available later in lifecycle)
     */
    public String renderConfigurationBindings(final BindingsConfig config) {
        return new ConfigBindingsRenderer(configurationTree).renderReport(config);
    }
}
