package ru.vyarus.dropwizard.guice.module.lifecycle.internal;

import com.google.inject.Injector;
import io.dropwizard.core.Configuration;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;
import org.glassfish.jersey.internal.inject.InjectionManager;
import ru.vyarus.dropwizard.guice.module.context.SharedConfigurationState;
import ru.vyarus.dropwizard.guice.module.context.option.Options;
import ru.vyarus.dropwizard.guice.module.yaml.ConfigurationTree;

/**
 * Context holds references to all objects, available to events.
 *
 * @author Vyacheslav Rusakov
 * @since 25.10.2019
 */
public class EventsContext {

    private final Options options;
    private final SharedConfigurationState sharedState;
    private Bootstrap bootstrap;
    private Configuration configuration;
    private ConfigurationTree configurationTree;
    private Environment environment;
    private Injector injector;
    private InjectionManager injectionManager;

    public EventsContext(final Options options, final SharedConfigurationState sharedState) {
        this.options = options;
        this.sharedState = sharedState;
    }

    public void setBootstrap(final Bootstrap bootstrap) {
        this.bootstrap = bootstrap;
    }

    public void setConfiguration(final Configuration configuration) {
        this.configuration = configuration;
    }

    public void setConfigurationTree(final ConfigurationTree configurationTree) {
        this.configurationTree = configurationTree;
    }

    public void setEnvironment(final Environment environment) {
        this.environment = environment;
    }

    public void setInjector(final Injector injector) {
        this.injector = injector;
    }

    public void setInjectionManager(final InjectionManager injectionManager) {
        this.injectionManager = injectionManager;
    }

    public Options getOptions() {
        return options;
    }

    public SharedConfigurationState getSharedState() {
        return sharedState;
    }

    public Bootstrap getBootstrap() {
        return bootstrap;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public ConfigurationTree getConfigurationTree() {
        return configurationTree;
    }

    public Environment getEnvironment() {
        return environment;
    }

    public Injector getInjector() {
        return injector;
    }

    public InjectionManager getInjectionManager() {
        return injectionManager;
    }
}
