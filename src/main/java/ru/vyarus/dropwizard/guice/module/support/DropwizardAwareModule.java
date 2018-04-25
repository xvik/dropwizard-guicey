package ru.vyarus.dropwizard.guice.module.support;

import com.google.inject.AbstractModule;
import io.dropwizard.Configuration;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import ru.vyarus.dropwizard.guice.module.context.option.Options;

/**
 * Base module to avoid boilerplate. It's not required to extend it, but
 * useful if dropwizard objects required in module: no need to manually implement interfaces.
 *
 * @param <C> configuration type
 * @author Vyacheslav Rusakov
 * @since 06.06.2015
 */
@SuppressWarnings("PMD.AvoidFieldNameMatchingMethodName")
public abstract class DropwizardAwareModule<C extends Configuration> extends AbstractModule implements
        EnvironmentAwareModule,
        BootstrapAwareModule<C>,
        ConfigurationAwareModule<C>,
        OptionsAwareModule {

    private C configuration;
    private Bootstrap<C> bootstrap;
    private Environment environment;
    private Options options;

    @Override
    public void setConfiguration(final C configuration) {
        this.configuration = configuration;
    }

    @Override
    public void setBootstrap(final Bootstrap<C> bootstrap) {
        this.bootstrap = bootstrap;
    }

    @Override
    public void setEnvironment(final Environment environment) {
        this.environment = environment;
    }

    @Override
    public void setOptions(final Options options) {
        this.options = options;
    }

    /**
     * @return application bootstrap object
     */
    protected Bootstrap<C> bootstrap() {
        return bootstrap;
    }

    /**
     * @return application configuration
     */
    protected C configuration() {
        return configuration;
    }

    /**
     * @return application environment
     */
    protected Environment environment() {
        return environment;
    }

    /**
     * @return application class package (most likely root package for entire application)
     */
    protected String appPackage() {
        return bootstrap().getApplication().getClass().getPackage().getName();
    }

    /**
     * @return options accessor object
     */
    protected Options options() {
        return options;
    }
}
