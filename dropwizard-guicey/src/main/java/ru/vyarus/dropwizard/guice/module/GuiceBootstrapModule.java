package ru.vyarus.dropwizard.guice.module;

import com.google.inject.Scopes;
import io.dropwizard.core.Configuration;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;
import ru.vyarus.dropwizard.guice.module.context.ConfigurationContext;
import ru.vyarus.dropwizard.guice.module.context.ConfigurationInfo;
import ru.vyarus.dropwizard.guice.module.context.option.Options;
import ru.vyarus.dropwizard.guice.module.context.option.OptionsInfo;
import ru.vyarus.dropwizard.guice.module.context.stat.StatsInfo;
import ru.vyarus.dropwizard.guice.module.installer.InstallerModule;
import ru.vyarus.dropwizard.guice.module.jersey.Jersey2Module;
import ru.vyarus.dropwizard.guice.module.support.DropwizardAwareModule;
import ru.vyarus.dropwizard.guice.module.support.scope.Prototype;
import ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule;

import javax.inject.Singleton;

/**
 * Bootstrap integration guice module.
 * <ul>
 * <li>Registers bootstrap, configuration and environment in injector</li>
 * <li>Installs jersey guice extension (to register resources instantiated with guice into jersey) and registers
 * guice filter</li>
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
 *
 * @param <T> configuration type
 * @author Vyacheslav Rusakov
 * @since 31.08.2014
 */
public class GuiceBootstrapModule<T extends Configuration> extends DropwizardAwareModule<T> {

    private final ConfigurationContext context;

    /**
     * Create bootstrap module.
     *
     * @param context configuration context
     */
    public GuiceBootstrapModule(final ConfigurationContext context) {
        this.context = context;
    }

    @Override
    protected void configure() {
        bindScope(Prototype.class, Scopes.NO_SCOPE);
        bindEnvironment();
        install(new InstallerModule(context));
        install(new Jersey2Module(bootstrap().getApplication(), environment(), context));

        // let guice beans use options the same way as bundles (with usage tracking)
        bind(Options.class).toInstance(options());

        // provide access for configuration info collected during startup
        bind(ConfigurationInfo.class).toInstance(new ConfigurationInfo(context));
        bind(StatsInfo.class).toInstance(new StatsInfo(context.stat()));
        bind(OptionsInfo.class).toInstance(new OptionsInfo(context.options()));
        bind(GuiceyConfigurationInfo.class).in(Singleton.class);
    }

    /**
     * Bind bootstrap, configuration and environment objects to be able to use them
     * as injectable.
     */
    private void bindEnvironment() {
        bind(Bootstrap.class).toInstance(bootstrap());
        bind(Environment.class).toInstance(environment());
        install(new ConfigBindingModule(configuration(), configurationTree()));
    }
}
