package ru.vyarus.dropwizard.guice.module;

import com.google.inject.AbstractModule;
import io.dropwizard.Configuration;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import ru.vyarus.dropwizard.guice.module.installer.InstallerModule;
import ru.vyarus.dropwizard.guice.module.installer.internal.InstallerConfig;
import ru.vyarus.dropwizard.guice.module.installer.scanner.ClasspathScanner;
import ru.vyarus.dropwizard.guice.module.jersey.Jersey2Module;
import ru.vyarus.dropwizard.guice.module.support.BootstrapAwareModule;
import ru.vyarus.dropwizard.guice.module.support.ConfigurationAwareModule;
import ru.vyarus.dropwizard.guice.module.support.EnvironmentAwareModule;

/**
 * Bootstrap integration guice module.
 * <ul>
 * <li>Registers bootstrap, configuration and environment in injector</li>
 * <li>Installs jersey guice extension (to register resources instantiated with guice into jersey) and registers
 * guice filter</li>
 * <li>Starts auto scanning, if enabled (for automatic features installation)</li>
 * </ul>
 *
 * @param <T> configuration type
 * @author Vyacheslav Rusakov
 * @since 31.08.2014
 */
public class GuiceSupportModule<T extends Configuration> extends AbstractModule
        implements BootstrapAwareModule<T>, EnvironmentAwareModule, ConfigurationAwareModule<T> {

    private final ClasspathScanner scanner;
    private final InstallerConfig installerConfig;
    private Bootstrap<T> bootstrap;
    private T configuration;
    private Environment environment;

    public GuiceSupportModule(final ClasspathScanner scanner,
                              final InstallerConfig installerConfig) {
        this.scanner = scanner;
        this.installerConfig = installerConfig;
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
        install(new InstallerModule(scanner, installerConfig));
        install(new Jersey2Module(environment));
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
