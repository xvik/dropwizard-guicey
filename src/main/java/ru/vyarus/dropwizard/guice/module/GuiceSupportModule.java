package ru.vyarus.dropwizard.guice.module;

import com.google.inject.AbstractModule;
import io.dropwizard.Configuration;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import ru.vyarus.dropwizard.guice.module.context.ConfigurationContext;
import ru.vyarus.dropwizard.guice.module.context.ConfigurationInfo;
import ru.vyarus.dropwizard.guice.module.installer.InstallerModule;
import ru.vyarus.dropwizard.guice.module.installer.scanner.ClasspathScanner;
import ru.vyarus.dropwizard.guice.module.jersey.Jersey2Module;
import ru.vyarus.dropwizard.guice.module.support.BootstrapAwareModule;
import ru.vyarus.dropwizard.guice.module.support.ConfigurationAwareModule;
import ru.vyarus.dropwizard.guice.module.support.EnvironmentAwareModule;

import javax.inject.Singleton;

/**
 * Bootstrap integration guice module.
 * <ul>
 * <li>Registers bootstrap, configuration and environment in injector</li>
 * <li>Installs jersey guice extension (to register resources instantiated with guice into jersey) and registers
 * guice filter</li>
 * <li>Starts auto scanning, if enabled (for automatic features installation)</li>
 * </ul>
 * Configuration is mapped as:
 * <ul>
 * <li>Root configuration class (e.g. {@code MyAppConfiguration extends Configuration})</li>
 * <li>Dropwizard {@link Configuration} class</li>
 * <li>All classes in hierarchy between root and {@link Configuration} (e.g.
 * {@code MyAppConfiguration extends MyBaseConfiguration extends Configuration}</li>
 * <li>All interfaces implemented directly by classes in configuration hierarchy except interfaces from
 * 'java' package (e.g. {@code MyBaseConfiguration implements HasMyOtherConfig})</li>
 * </ul>
 * Interface binding could be disabled using {@code bindConfigurationInterfaces} bundle option.
 *
 * @param <T> configuration type
 * @author Vyacheslav Rusakov
 * @since 31.08.2014
 */
public class GuiceSupportModule<T extends Configuration> extends AbstractModule
        implements BootstrapAwareModule<T>, EnvironmentAwareModule, ConfigurationAwareModule<T> {

    private final ClasspathScanner scanner;
    private final ConfigurationContext context;
    private final boolean bindConfigurationInterfaces;
    private Bootstrap<T> bootstrap;
    private T configuration;
    private Environment environment;

    public GuiceSupportModule(final ClasspathScanner scanner,
                              final ConfigurationContext context,
                              final boolean bindConfigurationInterfaces) {
        this.scanner = scanner;
        this.context = context;
        this.bindConfigurationInterfaces = bindConfigurationInterfaces;
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
        install(new InstallerModule(scanner, context));
        install(new Jersey2Module(bootstrap.getApplication(), environment));

        // provide access for configuration info collected during startup
        bind(ConfigurationInfo.class).toInstance(new ConfigurationInfo(context));
        bind(GuiceyConfigurationInfo.class).in(Singleton.class);
    }

    /**
     * Bind bootstrap, configuration and environment objects to be able to use them
     * as injectable.
     */
    private void bindEnvironment() {
        bind(Bootstrap.class).toInstance(bootstrap);
        bind(Environment.class).toInstance(environment);
        bindConfig(configuration.getClass());
    }

    /**
     * Bind configuration hierarchy: all superclasses and direct interfaces for each level (except interfaces
     * from java package).
     *
     * @param type configuration type
     */
    @SuppressWarnings("unchecked")
    private void bindConfig(final Class type) {
        bind(type).toInstance(configuration);
        if (type == Configuration.class) {
            return;
        }
        if (bindConfigurationInterfaces) {
            for (Class iface : type.getInterfaces()) {
                final String pkg = iface.getPackage().getName();
                if (pkg.startsWith("java.") || pkg.startsWith("groovy.")) {
                    continue;
                }
                bind(iface).toInstance(configuration);
            }
        }
        bindConfig(type.getSuperclass());
    }
}
