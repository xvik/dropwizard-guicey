package ru.vyarus.dropwizard.guice.module.lifecycle.internal;

import com.google.inject.Injector;
import io.dropwizard.core.Configuration;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;
import org.glassfish.jersey.internal.inject.InjectionManager;
import ru.vyarus.dropwizard.guice.module.context.SharedConfigurationState;
import ru.vyarus.dropwizard.guice.module.context.option.Options;
import ru.vyarus.dropwizard.guice.module.context.stat.StatsTracker;
import ru.vyarus.dropwizard.guice.module.yaml.ConfigurationTree;

/**
 * Context holds references to all objects, available to events.
 *
 * @author Vyacheslav Rusakov
 * @since 25.10.2019
 */
public class EventsContext {

    private final StatsTracker tracker;
    private final Options options;
    private final SharedConfigurationState sharedState;
    private Bootstrap bootstrap;
    private Configuration configuration;
    private ConfigurationTree configurationTree;
    private Environment environment;
    private Injector injector;
    private InjectionManager injectionManager;

    /**
     * Create events context.
     *
     * @param tracker     tracker
     * @param options     options
     * @param sharedState shared state
     */
    public EventsContext(final StatsTracker tracker,
                         final Options options,
                         final SharedConfigurationState sharedState) {
        this.tracker = tracker;
        this.options = options;
        this.sharedState = sharedState;
    }

    /**
     * @param bootstrap bootstrap
     */
    public void setBootstrap(final Bootstrap bootstrap) {
        this.bootstrap = bootstrap;
    }

    /**
     * @param configuration configuration
     */
    public void setConfiguration(final Configuration configuration) {
        this.configuration = configuration;
    }

    /**
     * @param configurationTree parsed configuration
     */
    public void setConfigurationTree(final ConfigurationTree configurationTree) {
        this.configurationTree = configurationTree;
    }

    /**
     * @param environment environment
     */
    public void setEnvironment(final Environment environment) {
        this.environment = environment;
    }

    /**
     * @param injector injector
     */
    public void setInjector(final Injector injector) {
        this.injector = injector;
    }

    /**
     * @param injectionManager injection manager
     */
    public void setInjectionManager(final InjectionManager injectionManager) {
        this.injectionManager = injectionManager;
    }

    /**
     * @return tracker
     */
    public StatsTracker getTracker() {
        return tracker;
    }

    /**
     * @return options
     */
    public Options getOptions() {
        return options;
    }

    /**
     * @return shared state
     */
    public SharedConfigurationState getSharedState() {
        return sharedState;
    }

    /**
     * @return bootstrap
     */
    public Bootstrap getBootstrap() {
        return bootstrap;
    }

    /**
     * @return configuration
     */
    public Configuration getConfiguration() {
        return configuration;
    }

    /**
     * @return parsed configuration
     */
    public ConfigurationTree getConfigurationTree() {
        return configurationTree;
    }

    /**
     * @return environment
     */
    public Environment getEnvironment() {
        return environment;
    }

    /**
     * @return injector
     */
    public Injector getInjector() {
        return injector;
    }

    /**
     * @return jersey injection manager
     */
    public InjectionManager getInjectionManager() {
        return injectionManager;
    }
}
