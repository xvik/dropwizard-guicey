package ru.vyarus.dropwizard.guice.module;

import com.google.inject.AbstractModule;
import io.dropwizard.Configuration;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import ru.vyarus.dropwizard.guice.module.autoconfig.AutoConfigModule;
import ru.vyarus.dropwizard.guice.module.autoconfig.feature.FeatureInstaller;
import ru.vyarus.dropwizard.guice.module.autoconfig.scanner.ClasspathScanner;
import ru.vyarus.dropwizard.guice.module.jersey.JerseyContainerModule;
import ru.vyarus.dropwizard.guice.module.support.BootstrapAwareModule;
import ru.vyarus.dropwizard.guice.module.support.ConfigurationAwareModule;
import ru.vyarus.dropwizard.guice.module.support.EnvironmentAwareModule;

import java.util.Collections;
import java.util.List;

/**
 * Bootstrap integration guice module.
 * <ul>
 * <li>Registers bootstrap, configuration and environment in injector</li>
 * <li>Installs jersey guice extension (to register resources instantiated with guice into jersey) and registers
 * guice filter</li>
 * <li>Starts auto scanning, if enabled (for automatic features installation)</li>
 * </ul>
 *
 * @author Vyacheslav Rusakov
 * @since 31.08.2014
 * @param <T> configuration type
 */
public class GuiceSupportModule<T extends Configuration> extends AbstractModule
        implements BootstrapAwareModule<T>, EnvironmentAwareModule, ConfigurationAwareModule<T> {

    private final ClasspathScanner scanner;
    private final List<Class<? extends FeatureInstaller>> disabledInstallers;
    private Bootstrap<T> bootstrap;
    private T configuration;
    private Environment environment;

    public GuiceSupportModule(final ClasspathScanner scanner,
                              final List<Class<? extends FeatureInstaller>> disabledInstallers) {
        this.scanner = scanner;
        this.disabledInstallers = disabledInstallers == null
                ? Collections.<Class<? extends FeatureInstaller>>emptyList()
                : disabledInstallers;
    }

    @Override
    public void setBootstrap(final Bootstrap<T> bootstrap) {
        this.bootstrap = bootstrap;
    }

    @Override
    public void setConfiguration(final T configuration) {
        this.configuration = configuration;
    }

    @Override
    public void setEnvironment(final Environment environment) {
        this.environment = environment;
    }

    @Override
    protected void configure() {
        bindEnvironment();
        install(new JerseyContainerModule());
        if (scanner != null) {
            install(new AutoConfigModule(scanner, disabledInstallers));
        }
    }

    /**
     * Bind bootstrap, configuration and environment objects to be able to use them
     * as injectable.
     */
    private void bindEnvironment() {
        bind(Bootstrap.class).toInstance(bootstrap);
        bind(Configuration.class).toInstance(configuration);
        @SuppressWarnings("unchecked")
        final Class<T> confClass = (Class<T>) configuration.getClass();
        if (confClass != Configuration.class) {
            bind(confClass).toInstance(configuration);
        }

        bind(Environment.class).toInstance(environment);
    }
}
