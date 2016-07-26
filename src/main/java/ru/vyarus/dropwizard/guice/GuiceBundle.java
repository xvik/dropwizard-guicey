package ru.vyarus.dropwizard.guice;


import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Stage;
import io.dropwizard.Configuration;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import ru.vyarus.dropwizard.guice.bundle.DefaultBundleLookup;
import ru.vyarus.dropwizard.guice.bundle.GuiceyBundleLookup;
import ru.vyarus.dropwizard.guice.bundle.lookup.VoidBundleLookup;
import ru.vyarus.dropwizard.guice.injector.DefaultInjectorFactory;
import ru.vyarus.dropwizard.guice.injector.InjectorFactory;
import ru.vyarus.dropwizard.guice.injector.lookup.InjectorLookup;
import ru.vyarus.dropwizard.guice.module.GuiceSupportModule;
import ru.vyarus.dropwizard.guice.module.context.ConfigurationContext;
import ru.vyarus.dropwizard.guice.module.context.debug.DiagnosticBundle;
import ru.vyarus.dropwizard.guice.module.installer.CoreInstallersBundle;
import ru.vyarus.dropwizard.guice.module.installer.FeatureInstaller;
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBundle;
import ru.vyarus.dropwizard.guice.module.installer.internal.CommandSupport;
import ru.vyarus.dropwizard.guice.module.installer.scanner.ClasspathScanner;
import ru.vyarus.dropwizard.guice.module.installer.util.BundleSupport;
import ru.vyarus.dropwizard.guice.module.jersey.debug.HK2DebugBundle;
import ru.vyarus.dropwizard.guice.module.support.BootstrapAwareModule;
import ru.vyarus.dropwizard.guice.module.support.ConfigurationAwareModule;
import ru.vyarus.dropwizard.guice.module.support.EnvironmentAwareModule;

import java.util.Arrays;
import java.util.Set;

/**
 * Bundle enables guice integration for dropwizard. Guice context is configured in initialization phase,
 * but actual injector is created on run phase, This approach provides greater configuration options, because during
 * initialization configuration and environment objects are not available. Bootstrap, Environment and Configuration
 * object will be available in juice context. But if you need them in module (for example to get
 * configuration parameters), implement one of *AwareModule interfaces (e.g.
 * {@link ru.vyarus.dropwizard.guice.module.support.ConfigurationAwareModule}).
 * <p>
 * You can use auto scan to automatically install features. To enable auto scan you must configure package (or
 * packages) to search in. To know all supported features look
 * {@link ru.vyarus.dropwizard.guice.module.installer.FeatureInstaller} implementations. Installers are
 * expendable mechanism: they are resolved by scanning classpath, so you can add new installers in your code and
 * classpath scanning will find them and activate. Also, features could be disabled (for example, if you want to
 * replace existing feature, you will disable it in builder and implement your own - auto scan will find it and
 * activate).
 * <p>
 * Any class may be hidden from auto scanning with {@code @InvisibleForScanner} annotation.
 * <p>
 * Commands may use injection too, but only fields injection. You can register command manually and their fields
 * will be injected or you can activate auto scan for commands in builder (disabled by default). If auto scan
 * for commands enabled, they will be instantiated with default no-arg constructor.
 * <p>
 * Resources are registered using jersey integration module. GuiceFilter is also registered.
 * <p>
 * Lifecycle:
 * <ul>
 * <li>Bundle configured</li>
 * <li>Bundle initialization started</li>
 * <li>If commands scan enabled, commands resolved from classpath and registered in Bootstrap</li>
 * <li>Bundle run started</li>
 * <li>If auto scan enabled, scan classpath for feature installers and perform one more scan with registered
 * installers to find extensions</li>
 * <li>Perform {@link GuiceyBundle} lookup with registered {@link GuiceyBundleLookup}</li>
 * <li>Guice injector created</li>
 * <li>Register all extensions found by installers</li>
 * <li>Perform injections for all registered environment commands (because only environment commands runs bundles)</li>
 * </ul>
 * <p>
 * Project is based on ideas from <a href="https://github.com/HubSpot/dropwizard-guice">dropwizard-guice</a>
 * project. And because of this project name was changed to dropwizard-guicey.
 *
 * @param <T> configuration type
 * @author Vyacheslav Rusakov
 * @see ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo for configuratio diagnostic
 * @since 31.08.2014
 */
@SuppressWarnings({"PMD.ExcessiveImports", "PMD.TooManyMethods"})
public final class GuiceBundle<T extends Configuration> implements ConfiguredBundle<T> {

    private Injector injector;
    private final ConfigurationContext context = new ConfigurationContext();
    private final Set<String> autoscanPackages = Sets.newHashSet();
    private InjectorFactory injectorFactory = new DefaultInjectorFactory();
    private GuiceyBundleLookup bundleLookup = new DefaultBundleLookup();
    private boolean searchCommands;
    private boolean configureFromDropwizardBundles;
    private boolean bindConfigurationInterfaces = true;
    private Stage stage = Stage.PRODUCTION;

    private Bootstrap bootstrap;
    private ClasspathScanner scanner;

    GuiceBundle() {
        // Bundle should be instantiated only from builder
    }

    @Override
    public void initialize(final Bootstrap bootstrap) {
        if (searchCommands) {
            Preconditions.checkState(!autoscanPackages.isEmpty(),
                    "Commands search could not be performed, because auto scan was not activated");
        }
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
        configureFromBundles(configuration, environment);
        context.registerModules(new GuiceSupportModule(
                scanner, context, bindConfigurationInterfaces));
        configureModules(configuration, environment);
        injector = injectorFactory.createInjector(stage, context.getModules());
        // registering as managed to cleanup injector on application stop
        environment.lifecycle().manage(
                InjectorLookup.registerInjector(bootstrap.getApplication(), injector));
        CommandSupport.initCommands(bootstrap.getCommands(), injector);
    }

    /**
     * @return created injector instance or fail if injector not yet created
     */
    public Injector getInjector() {
        return Preconditions.checkNotNull(injector, "Guice not initialized");
    }

    /**
     * Apply configuration from registered bundles. If dropwizard bundles support is enabled, lookup them too.
     *
     * @param configuration configuration object
     * @param environment   environment object
     */
    private void configureFromBundles(final T configuration, final Environment environment) {
        if (configureFromDropwizardBundles) {
            context.registerDwBundles(BundleSupport.findBundles(bootstrap, GuiceyBundle.class));
        }
        context.registerLookupBundles(bundleLookup.lookup());
        BundleSupport.processBundles(context, configuration, environment, bootstrap.getApplication());
    }

    /**
     * Post-process registered modules by injecting bootstrap, configuration and environment objects.
     *
     * @param configuration configuration object
     * @param environment   environment object
     */
    @SuppressWarnings("unchecked")
    private void configureModules(final T configuration, final Environment environment) {
        for (Module mod : context.getModules()) {
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
         * Configures custom {@link InjectorFactory}. Required by some guice extensions like governator.
         *
         * @param injectorFactory custom guice injector factory
         * @return builder instance for chained calls
         */
        public Builder<T> injectorFactory(final InjectorFactory injectorFactory) {
            bundle.injectorFactory = injectorFactory;
            return this;
        }

        /**
         * Configure custom {@link GuiceyBundleLookup}. Lookup provides an easy way to indirectly install
         * {@link GuiceyBundle} bundles. Default implementation support lookup by system property.
         *
         * @param bundleLookup custom bundle lookup implementation
         * @return builder instance for chained calls
         * @see DefaultBundleLookup
         */
        public Builder<T> bundleLookup(final GuiceyBundleLookup bundleLookup) {
            bundle.bundleLookup = bundleLookup;
            return this;
        }

        /**
         * Disables default bundle lookup.
         *
         * @return builder instance for chained calls
         */
        public Builder<T> disableBundleLookup() {
            return bundleLookup(new VoidBundleLookup());
        }

        /**
         * Enables auto scan feature.
         * When enabled, all core installers are registered automatically.
         *
         * @param basePackages packages to scan extensions in
         * @return builder instance for chained calls
         */
        public Builder<T> enableAutoConfig(final String... basePackages) {
            Preconditions.checkState(bundle.autoscanPackages.isEmpty(), "Auto config packages already configured");
            Preconditions.checkState(basePackages.length > 0, "Specify at least one package to scan");
            bundle.autoscanPackages.addAll(Arrays.asList(basePackages));
            bundle.context.registerBundles(new CoreInstallersBundle());
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
         * @see ru.vyarus.dropwizard.guice.module.support.DropwizardAwareModule
         */
        public Builder<T> modules(final Module... modules) {
            Preconditions.checkState(modules.length > 0, "Specify at least one module");
            bundle.context.registerModules(modules);
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
         * @param installers feature installer types to disable
         * @return builder instance for chained calls
         */
        @SafeVarargs
        public final Builder<T> disableInstallers(final Class<? extends FeatureInstaller>... installers) {
            bundle.context.disableInstallers(installers);
            return this;
        }

        /**
         * Feature installers registered automatically when auto scan enabled,
         * but if you don't want to use it, you can register installers manually (note: without auto scan default
         * installers will not be registered).
         * <p>Also, could be used to add installers from packages not included in auto scanning.</p>
         *
         * @param installers feature installer classes to register
         * @return builder instance for chained calls
         */
        @SafeVarargs
        public final Builder<T> installers(final Class<? extends FeatureInstaller>... installers) {
            bundle.context.registerInstallers(installers);
            return this;
        }

        /**
         * Beans could be registered automatically when auto scan enabled,
         * but if you don't want to use it, you can register beans manually.
         * <p>Guice injector will instantiate beans and registered installers will be used to recognize and
         * properly register provided extension beans.</p>
         * <p>Also, could be used to add beans from packages not included in auto scanning.</p>
         * <p>NOTE: startup will fail if bean not recognized by installers.</p>
         * <p>NOTE: Don't register commands here: either enable auto scan, which will install commands automatically
         * or register command directly to bootstrap object and dependencies will be injected to them after
         * injector creation.</p>
         *
         * @param extensionClasses extension bean classes to register
         * @return builder instance for chained calls
         */
        public Builder<T> extensions(final Class<?>... extensionClasses) {
            bundle.context.registerExtensions(extensionClasses);
            return this;
        }

        /**
         * Guicey bundles are mainly useful for extensions (to group installers and extensions installation without
         * auto scan). Its very like dropwizard bundles.
         * <p>
         * Its also possible to use dropwizard bundles as guicey bundles: bundle must implement
         * {@link GuiceyBundle} and {@link #configureFromDropwizardBundles(boolean)} must be enabled
         * (disabled by default). This allows using dropwizard bundles as universal extension point.
         * <p>
         * Duplicate bundles are filtered automatically: bundles of the same type considered duplicate.
         *
         * @param bundles guicey bundles
         * @return builder instance for chained calls
         */
        public Builder<T> bundles(final GuiceyBundle... bundles) {
            bundle.context.registerBundles(bundles);
            return this;
        }

        /**
         * If enabled registered dropwizard bundles are checked if they implement {@link GuiceyBundle} and called
         * to configure guice. This allows using dropwizard bundles as universal extension point.
         * <p>Disabled by default.</p>
         *
         * @param enable true to enable configuration from dropwizard bundles
         * @return builder instance for chained calls
         */
        public Builder<T> configureFromDropwizardBundles(final boolean enable) {
            bundle.configureFromDropwizardBundles = enable;
            return this;
        }

        /**
         * If enabled, interfaces implemented by configuration will also be bound to configuration instance
         * in to guice context. Only interfaces directly implemented by any configuration class in configuration
         * classes hierarchy. Interfaces from java and groovy packages are skipped.
         * This is useful to support {@code HasSomeConfiguration} interfaces convention.
         * <p>
         * When disabled, only classes in configuration hierarchy are registered (e.g. in case
         * {@code MyConfiguration extends MyBaseConfiguration extends Configuration}, all 3 classes would be bound.
         * <p>
         * Enabled by default.
         *
         * @param enable true to enable configuration interfaces binding
         * @return builder instance for chained calls
         */
        public Builder<T> bindConfigurationInterfaces(final boolean enable) {
            bundle.bindConfigurationInterfaces = enable;
            return this;
        }

        /**
         * Enables strict control of beans instantiation context: all beans must be instantiated by guice, except
         * beans annotated with {@link ru.vyarus.dropwizard.guice.module.installer.feature.jersey.HK2Managed}.
         * When bean instantiated in wrong context exception would be thrown.
         * <p>
         * It is useful if you write your own installers or to simply ensure correctness in doubtful cases.
         * <p>
         * Do not use for production! It is intended to be used mostly in tests or to diagnose problems
         * during development.
         * <p>
         * To implicitly enable this check in all tests use
         * {@code PropertyBundleLookup.enableBundles(HK2DebugBundle.class)}.
         *
         * @return builder instance for chained calls
         * @see HK2DebugBundle
         */
        public Builder<T> strictScopeControl() {
            bundle.context.registerBundles(new HK2DebugBundle());
            return this;
        }

        /**
         * Print additional diagnostic logs with installed bundles, installers and resolved extensions and
         * configuration tree.
         * <p>
         * Useful for configuration problems resolution and for better understanding how guicey works.
         * <p>
         * If custom logging format is required use {@link DiagnosticBundle} directly.
         * <p>
         * Also, bundle could be enabled with bundle lookup mechanism (e.g. with system property, see
         * {@link GuiceyBundleLookup}).
         *
         * @return builder instance for chained calls
         * @see DiagnosticBundle
         */
        public Builder<T> printDiagnosticInfo() {
            bundle.context.registerBundles(new DiagnosticBundle());
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
         * @return bundle instance with implicit PRODUCTION stage
         */
        public GuiceBundle<T> build() {
            return build(Stage.PRODUCTION);
        }
    }
}
