package ru.vyarus.dropwizard.guice;


import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Stage;
import io.dropwizard.Configuration;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import ru.vyarus.dropwizard.guice.module.GuiceSupportModule;
import ru.vyarus.dropwizard.guice.module.autoconfig.feature.FeatureInstaller;
import ru.vyarus.dropwizard.guice.module.autoconfig.scanner.ClasspathScanner;
import ru.vyarus.dropwizard.guice.module.autoconfig.util.CommandSupport;
import ru.vyarus.dropwizard.guice.module.support.BootstrapAwareModule;
import ru.vyarus.dropwizard.guice.module.support.ConfigurationAwareModule;
import ru.vyarus.dropwizard.guice.module.support.EnvironmentAwareModule;

import java.util.Arrays;
import java.util.List;

/**
 * Bundle enables guice integration for dropwizard. Guice context is configured in initialization phase,
 * but actual injector is created on run phase, This approach provides greater configuration options, because during
 * initialization configuration and environment objects are not available. Bootstrap, Environment and Configuration
 * object will be available in juice context. But if you need them in module (for example to get
 * configuration parameters), implement one of *AwareModule interfaces (e.g.
 * {@link ru.vyarus.dropwizard.guice.module.support.ConfigurationAwareModule}).
 * <p>You can use auto scan to automatically install features. To enable auto scan you must configure package (or
 * packages) to search in. To know all supported features look
 * {@link ru.vyarus.dropwizard.guice.module.autoconfig.feature.FeatureInstaller} implementations. Installers are
 * expendable mechanism: they are resolved by scanning classpath, so you can add new installers in your code and
 * classpath scanning will find them and activate. Also, features could be disabled (for example, if you want to
 * replace existing feature, you will disable it in builder and implement your own - auto scan will find it and
 * activate).</p>
 * <p>Any class may be hidden from auto scanning with {@code @InvisibleForScanner} annotation.</p>
 * <p>Commands may use injection too, but only fields injection. You can register command manually and their fields
 * will be injected or you can activate auto scan for commands in builder (disabled by default). If auto scan
 * for commands enabled, they will be instantiated with default no-arg constructor.</p>
 * <p>Resources are registered using jersey integration module. GuiceFilter is also registered.</p>
 * Lifecycle:
 * <ul>
 * <li>Bundle configured</li>
 * <li>Bundle initialization started</li>
 * <li>If commands scan enabled, commands resolved from classpath and registered in Bootstrap</li>
 * <li>Bundle run started</li>
 * <li>If auto scan enabled, scan classpath for feature installers and perform one more scan with registered
 * installers to find extensions</li>
 * <li>Guice injector created</li>
 * <li>Register all extensions found by installers</li>
 * <li>Perform injections for all registered commands</li>
 * </ul>
 * <p>Project is based on ideas from <a href="https://github.com/HubSpot/dropwizard-guice">dropwizard-guice</a>
 * project</p>. And because of this project name was changed to dropwizard-guicey.
 *
 * @author Vyacheslav Rusakov
 * @since 31.08.2014
 * @param <T> configuration type
 */
public final class GuiceBundle<T extends Configuration> implements ConfiguredBundle<T> {

    private static Injector injector;

    private final List<Module> modules = Lists.newArrayList();
    private final List<String> autoscanPackages = Lists.newArrayList();
    private final List<Class<? extends FeatureInstaller>> disabledInstallers = Lists.newArrayList();
    private boolean searchCommands;
    private Stage stage = Stage.PRODUCTION;

    private Bootstrap bootstrap;
    private ClasspathScanner scanner;

    GuiceBundle() {
        // Bundle should be instantiated only from builder
    }

    @Override
    public void initialize(final Bootstrap bootstrap) {
        // have to remember bootstrap in order to
        this.bootstrap = bootstrap;
        // init scanner
        if (!autoscanPackages.isEmpty()) {
            scanner = new ClasspathScanner(autoscanPackages);
            if (searchCommands) {
                CommandSupport.registerCommands(bootstrap, scanner);
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void run(final T configuration, final Environment environment) throws Exception {
        modules.add(new GuiceSupportModule(scanner, disabledInstallers));
        configureModules(configuration, environment);
        GuiceBundle.injector = Guice.createInjector(stage, modules);
        CommandSupport.initCommands(bootstrap.getCommands(), injector);
    }

    /**
     * @return created injector instance or fail if injector not yet created
     */
    public static Injector getInjector() {
        return Preconditions.checkNotNull(injector, "Guice not initialized");
    }

    /**
     * Post-process registered modules by injecting bootstrap, configuration and environment objects.
     *
     * @param configuration configuration object
     * @param environment   environment object
     */
    @SuppressWarnings("unchecked")
    private void configureModules(final T configuration, final Environment environment) {
        for (Module mod : modules) {
            if (mod instanceof BootstrapAwareModule) {
                ((BootstrapAwareModule) mod).setBootstrap(bootstrap);
            }
            if (mod instanceof ConfigurationAwareModule) {
                ((ConfigurationAwareModule<T>) mod).setConfiguration(configuration);
            }
            if (mod instanceof EnvironmentAwareModule) {
                ((EnvironmentAwareModule) mod).setEnvironment(environment);
            }
        }
    }

    /**
     * @param <T> configuration type
     * @return builder instance to construct bundle
     */
    public static <T extends Configuration> Builder<T> builder() {
        return new Builder<T>();
    }

    /**
     * Builder encapsulates bundle configuration options.
     *
     * @param <T> configuration type
     */
    public static class Builder<T extends Configuration> {
        private final GuiceBundle<T> bundle = new GuiceBundle<T>();

        /**
         * Enables auto scan feature.
         *
         * @param basePackages packages to scan extensions in
         * @return builder instance for chained calls
         */
        public Builder<T> enableAutoConfig(final String... basePackages) {
            Preconditions.checkState(bundle.autoscanPackages.isEmpty(), "Auto config packages already configured");
            Preconditions.checkState(basePackages.length > 0, "Specify at least one package to scan");
            bundle.autoscanPackages.addAll(Arrays.asList(basePackages));
            // adding special package with predefined feature installers
            bundle.autoscanPackages.add("ru.vyarus.dropwizard.guice.module.autoconfig.feature");
            return this;
        }

        /**
         * @param installers installer types to disable
         * @return builder instance for chained calls
         */
        public Builder<T> disableInstallers(final Class<? extends FeatureInstaller>... installers) {
            bundle.disabledInstallers.addAll(Arrays.asList(installers));
            return this;
        }

        /**
         * NOTE: if module implements *AwareModule interfaces, objects will be set just before configuration start.
         *
         * @param modules one or more juice modules
         * @return builder instance for chained calls
         * @see ru.vyarus.dropwizard.guice.module.support.BootstrapAwareModule
         * @see ru.vyarus.dropwizard.guice.module.support.ConfigurationAwareModule
         * @see ru.vyarus.dropwizard.guice.module.support.EnvironmentAwareModule
         */
        public Builder<T> modules(final Module... modules) {
            Preconditions.checkState(modules.length > 0, "Specify at least one module");
            bundle.modules.addAll(Arrays.asList(modules));
            return this;
        }

        /**
         * NOTE: will not scan if auto scan not enabled (packages not configured).
         *
         * @param searchCommands true to enable class path scanning for commands, false to disable (default)
         * @return builder instance for chained calls
         */
        public Builder<T> searchCommands(final boolean searchCommands) {
            bundle.searchCommands = searchCommands;
            return this;
        }

        /**
         * @param stage stage to run injector with
         * @return bundle instance
         */
        public GuiceBundle<T> build(final Stage stage) {
            bundle.stage = stage;
            return bundle;
        }

        /**
         * Stage implicitly set to PRODUCTION.
         *
         * @return bundle instance
         */
        public GuiceBundle<T> build() {
            return build(Stage.PRODUCTION);
        }
    }
}
