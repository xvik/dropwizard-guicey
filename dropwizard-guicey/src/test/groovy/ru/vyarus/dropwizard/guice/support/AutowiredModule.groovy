package ru.vyarus.dropwizard.guice.support

import com.google.inject.AbstractModule
import io.dropwizard.core.Configuration
import io.dropwizard.core.setup.Bootstrap
import io.dropwizard.core.setup.Environment
import ru.vyarus.dropwizard.guice.module.support.BootstrapAwareModule
import ru.vyarus.dropwizard.guice.module.support.ConfigurationAwareModule
import ru.vyarus.dropwizard.guice.module.support.EnvironmentAwareModule
import ru.vyarus.dropwizard.guice.module.support.ConfigurationTreeAwareModule
import ru.vyarus.dropwizard.guice.module.yaml.ConfigurationTree
import ru.vyarus.dropwizard.guice.support.feature.InvisibleResource

/**
 * Check dropwizard objects autowiring and implicit resource registration
 * @author Vyacheslav Rusakov 
 * @since 04.09.2014
 */
class AutowiredModule extends AbstractModule implements BootstrapAwareModule<Configuration>,
        EnvironmentAwareModule, ConfigurationAwareModule<Configuration>, ConfigurationTreeAwareModule {

    static AutowiredModule instance

    Bootstrap bootstrap
    Environment environment
    Configuration configuration
    ConfigurationTree configurationTree

    AutowiredModule() {
        instance = this;
    }

    @Override
    protected void configure() {
        // resource not visible for classpath scan, but we bind it manually and
        // jersey-guice will register it
        bind(InvisibleResource)
    }

    @Override
    void setBootstrap(Bootstrap<Configuration> bootstrap) {
        this.bootstrap = bootstrap
    }

    @Override
    void setConfiguration(Configuration configuration) {
        this.configuration = configuration
    }

    @Override
    void setEnvironment(Environment environment) {
        this.environment = environment
    }

    @Override
    void setConfigurationTree(ConfigurationTree configurationTree) {
        this.configurationTree = configurationTree
    }
}
