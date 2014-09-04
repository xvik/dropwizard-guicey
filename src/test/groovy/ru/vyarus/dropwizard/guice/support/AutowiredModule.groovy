package ru.vyarus.dropwizard.guice.support

import com.google.inject.AbstractModule
import io.dropwizard.Configuration
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import ru.vyarus.dropwizard.guice.module.support.BootstrapAwareModule
import ru.vyarus.dropwizard.guice.module.support.ConfigurationAwareModule
import ru.vyarus.dropwizard.guice.module.support.EnvironmentAwareModule
import ru.vyarus.dropwizard.guice.support.feature.InvisibleResource

/**
 * Check dropwizard objects autowiring and implicit resource registration
 * @author Vyacheslav Rusakov 
 * @since 04.09.2014
 */
class AutowiredModule extends AbstractModule implements BootstrapAwareModule<Configuration>,
        EnvironmentAwareModule, ConfigurationAwareModule<Configuration> {

    static AutowiredModule instance

    Bootstrap bootstrap
    Environment environment
    Configuration configuration

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
}
