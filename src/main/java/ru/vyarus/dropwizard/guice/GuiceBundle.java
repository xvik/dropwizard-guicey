package ru.vyarus.dropwizard.guice;


import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
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
import ru.vyarus.dropwizard.guice.module.context.debug.report.diagnostic.DiagnosticConfig;
import ru.vyarus.dropwizard.guice.module.context.debug.report.tree.ContextTreeConfig;
import ru.vyarus.dropwizard.guice.module.context.option.Option;
import ru.vyarus.dropwizard.guice.module.installer.CoreInstallersBundle;
import ru.vyarus.dropwizard.guice.module.installer.FeatureInstaller;
import ru.vyarus.dropwizard.guice.module.installer.WebInstallersBundle;
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBundle;
import ru.vyarus.dropwizard.guice.module.installer.internal.CommandSupport;
import ru.vyarus.dropwizard.guice.module.installer.scanner.ClasspathScanner;
import ru.vyarus.dropwizard.guice.module.installer.util.BundleSupport;
import ru.vyarus.dropwizard.guice.module.jersey.debug.HK2DebugBundle;
import ru.vyarus.dropwizard.guice.module.support.BootstrapAwareModule;
import ru.vyarus.dropwizard.guice.module.support.ConfigurationAwareModule;
import ru.vyarus.dropwizard.guice.module.support.EnvironmentAwareModule;

import javax.servlet.DispatcherType;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Map;

import static ru.vyarus.dropwizard.guice.GuiceyOptions.*;
import static ru.vyarus.dropwizard.guice.module.context.stat.Stat.*;

/**
 * Bundle enables guice integration for dropwizard. Guice context is configured in initialization phase,
 * but actual injector is created on run phase, This approach provides greater configuration options, because during
 * initialization configuration and environment objects are not available. Bootstrap, Environment and Configuration
 * objects will be available in guice context. But if you need them in module (for example to get
 * configuration parameters), implement one of *AwareModule interfaces (e.g.
 * {@link ru.vyarus.dropwizard.guice.module.support.ConfigurationAwareModule}).
 * <p>
 * You can use auto scan to automatically install features. To enable auto scan you must configure package (or
 * packages) to search in. To know all supported features look
 * {@link ru.vyarus.dropwizard.guice.module.installer.FeatureInstaller} implementations. Installers are
 * extendable mechanism: they are resolved by scanning classpath, so you can add new installers in your code and
 * classpath scanning will find them and activate. Also, installers could be disabled (for example, if you want to
 * replace existing installer, you will disable it in builder and implement your own - auto scan will find it and
 * activate).
 * <p>
 * Any class may be hidden from auto scanning with {@code @InvisibleForScanner} annotation.
 * <p>
 * Commands may use injection too, but only fields injection. You can register command manually and their fields
 * will be injected or you can activate auto scan for commands in builder (disabled by default). If auto scan
 * for commands enabled, they will be instantiated with default no-arg constructor.
 * <p>
 * Resources are registered using jersey integration module. GuiceFilter is registered for both contexts to provide
 * request and session scopes support.
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
 * Project was originally inspired by <a href="https://github.com/HubSpot/dropwizard-guice">dropwizard-guice</a>
 * project. And because of this, project name was changed to dropwizard-guicey.
 *
 * @param <T> configuration type
 * @author Vyacheslav Rusakov
 * @see ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo for configuratio diagnostic
 * @since 31.08.2014
 */
@SuppressWarnings({"PMD.ExcessiveImports", "PMD.TooManyMethods", "PMD.ExcessiveClassLength"})
public final class GuiceBundle<T extends Configuration> implements ConfiguredBundle<T> {

    private Injector injector;
    private final ConfigurationContext context = new ConfigurationContext();
    private InjectorFactory injectorFactory = new DefaultInjectorFactory();
    private GuiceyBundleLookup bundleLookup = new DefaultBundleLookup();

    private Bootstrap bootstrap;
    private ClasspathScanner scanner;

    GuiceBundle() {
        // Bundle should be instantiated only from builder
    }

    @Override
    public void initialize(final Bootstrap bootstrap) {
        final Stopwatch timer = context.stat().timer(GuiceyTime);
        final String[] packages = context.option(ScanPackages);
        final boolean searchCommands = context.option(SearchCommands);
        final boolean scanEnabled = packages.length > 0;
        if (searchCommands) {
            Preconditions.checkState(scanEnabled,
                    "Commands search could not be performed, because auto scan was not activated");
        }
        // have to remember bootstrap in order to inject it into modules
        this.bootstrap = bootstrap;
        if (scanEnabled) {
            scanner = new ClasspathScanner(Sets.newHashSet(Arrays.asList(packages)), context.stat());
            if (searchCommands) {
                CommandSupport.registerCommands(bootstrap, scanner, context);
            }
        }
        timer.stop();
    }

    @Override
    public void run(final T configuration, final Environment environment) throws Exception {
        final Stopwatch timer = context.stat().timer(GuiceyTime);
        if (context.option(UseCoreInstallers)) {
            context.registerBundles(new CoreInstallersBundle());
        }
        configureFromBundles(configuration, environment);
        context.registerModules(new GuiceSupportModule(scanner, context));
        configureModules(configuration, environment);
        createInjector(environment);
        afterInjectorCreation();
        timer.stop();
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
        final Stopwatch timer = context.stat().timer(BundleTime);
        final Stopwatch resolutionTimer = context.stat().timer(BundleResolutionTime);
        if (context.option(ConfigureFromDropwizardBundles)) {
            context.registerDwBundles(BundleSupport.findBundles(bootstrap, GuiceyBundle.class));
        }
        context.registerLookupBundles(bundleLookup.lookup());
        resolutionTimer.stop();
        BundleSupport.processBundles(context, configuration, environment, bootstrap.getApplication());
        timer.stop();
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

    private void createInjector(final Environment environment) {
        final Stopwatch timer = context.stat().timer(InjectorCreationTime);
        injector = injectorFactory.createInjector(context.option(InjectorStage), context.getModules());
        // registering as managed to cleanup injector on application stop
        environment.lifecycle().manage(
                InjectorLookup.registerInjector(bootstrap.getApplication(), injector));
        timer.stop();
    }

    @SuppressWarnings("unchecked")
    private void afterInjectorCreation() {
        CommandSupport.initCommands(bootstrap.getCommands(), injector, context.stat());
        if (scanner != null) {
            scanner.cleanup();
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
         * Options is a generic mechanism to provide internal configuration values for guicey and 3rd party bundles.
         * See {@link GuiceyOptions} as options example. Bundles may define their own enums in the same way to
         * use options mechanism.
         * <p>
         * Options intended to be used for development time specific configurations (most likely
         * low level options to slightly change behaviour). Also, in contrast to internal booleans
         * (e.g. in main bundle), options are accessible everywhere and may be used by other 3rd party module or
         * simply for reporting.
         * <p>
         * Options may be set only on application level. Guicey bundles could access option values through
         * {@linkplain ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBootstrap#option(Enum)
         * bootstrap object}. Guice service could access options through
         * {@linkplain ru.vyarus.dropwizard.guice.module.context.option.Options options bean}. Installers
         * could use {@link ru.vyarus.dropwizard.guice.module.installer.option.WithOptions} to get access to options.
         * Options metadata for reporting is available through
         * {@linkplain ru.vyarus.dropwizard.guice.module.context.option.OptionsInfo guice bean}.
         * <p>
         * Options may be used instead of shortcut methods when configuration value is dynamic (without options
         * it would not be impossible to configure without breaking builder flow and additional if statements).
         * <p>
         * Note: each option declares exact option type and provided value is checked for type compatibility.
         *
         * @param option option enum
         * @param value  option value (not null)
         * @param <K>    helper type for option signature definition
         * @return builder instance for chained calls
         * @throws NullPointerException     is null value provided
         * @throws IllegalArgumentException if provided value incompatible with option type
         * @see Option for more details
         * @see GuiceyOptions
         * @see ru.vyarus.dropwizard.guice.module.installer.InstallersOptions
         */
        public <K extends Enum & Option> Builder<T> option(final K option, final Object value) {
            bundle.context.setOption(option, value);
            return this;
        }

        /**
         * Sets multiple options at once. No options lookup mechanism provided out of the box (like for bundles),
         * but it's very easy to implement custom lookup solution (e.g. for providing specific options in tests).
         * This method would be useful for such lookup implementations.
         * <p>
         * Note: {@link Option} type is not mixed with enum in declaration to simplify usage,
         * but used enums must be correct options.
         *
         * @param options options map (not null)
         * @param <K>     helper type for option signature definition
         * @return builder instance for chained calls
         * @throws NullPointerException     is null value provided for any option
         * @throws IllegalArgumentException if any provided value incompatible with option type
         * @see #option(Enum, Object) for more info
         */
        @SuppressWarnings("unchecked")
        public <K extends Enum & Option> Builder<T> options(final Map<Enum, Object> options) {
            ((Map<K, Object>) (Map) options).forEach(this::option);
            return this;
        }

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
         * @see GuiceyOptions#ScanPackages
         */
        public Builder<T> enableAutoConfig(final String... basePackages) {
            Preconditions.checkState(basePackages.length > 0, "Specify at least one package to scan");
            return option(ScanPackages, basePackages);
        }

        /**
         * All registered modules must be of unique type (all modules registered). If two or more modules of the
         * same type registered, only first instance will be used.
         * <p>
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
         * NOTE: will not scan if auto scan not enabled (packages not configured
         * with {@link #enableAutoConfig(String...)}).
         * <p>
         * Enables commands classpath search. All found commands are instantiated and registered in
         * bootstrap. Default constructor is used for simple commands, but {@link io.dropwizard.cli.EnvironmentCommand}
         * must have constructor with {@link io.dropwizard.Application} argument.
         * <p>
         * By default, commands search is disabled.
         *
         * @return builder instance for chained calls
         * @see CommandSupport
         * @see GuiceyOptions#SearchCommands
         */
        public Builder<T> searchCommands() {
            return option(SearchCommands, true);
        }

        /**
         * Disables automatic {@link CoreInstallersBundle} registration (no installers will be registered by default).
         *
         * @return builder instance for chained calls
         * @see GuiceyOptions#UseCoreInstallers
         */
        public Builder<T> noDefaultInstallers() {
            return option(UseCoreInstallers, false);
        }

        /**
         * Shortcut to install {@link WebInstallersBundle}. Web installers are not available by default to
         * reduce default installers count. Web installers use default servlet api annotations to install
         * guice-aware servlets, filters and listeners. In many cases it will be more useful than using
         * guice servlet modules.
         * <p>
         * When use web installers to declare servlets and filters, guice servlet modules support may be disabled
         * with {@link #noGuiceFilter()}. This will safe some startup time and removed extra guice filters.
         *
         * @return builder instance for chained calls
         * @see WebInstallersBundle
         */
        public Builder<T> useWebInstallers() {
            bundle.context.registerBundles(new WebInstallersBundle());
            return this;
        }

        /**
         * Disables {@link com.google.inject.servlet.GuiceFilter} registration for both application and admin contexts.
         * Without guice filter registered, guice {@link com.google.inject.servlet.ServletModule} registrations
         * are useless (because they can't be used). So guice servlet modules support will be disabled:
         * no request or session scopes may be used and registrations of servlet modules will be denied
         * (binding already declared exception). Even with disabled guice filter, request and response
         * objects provider injections still may be used in resources (will work through hk provider).
         * <p>
         * Guice servlets initialization took ~50ms, so injector creation will be a bit faster after disabling.
         * <p>
         * Enable {@link #useWebInstallers()} web installers to use instead of guice servlet modules for servlets
         * and filters registration.
         *
         * @return builder instance for chained calls
         * @see GuiceyOptions#GuiceFilterRegistration
         */
        public Builder<T> noGuiceFilter() {
            return option(GuiceFilterRegistration, EnumSet.noneOf(DispatcherType.class));
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
         * or register command directly to bootstrap object and dependencies will be injected to it after
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
         * It's also possible to use dropwizard bundles as guicey bundles: bundle must implement
         * {@link GuiceyBundle} and {@link #configureFromDropwizardBundles()} must be enabled
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
         * Enables registered dropwizard bundles check if they implement {@link GuiceyBundle} and register them as
         * guicey bundles. This allows using dropwizard bundles as universal extension point.
         * <p>
         * Disabled by default.
         *
         * @return builder instance for chained calls
         * @see GuiceyOptions#ConfigureFromDropwizardBundles
         */
        public Builder<T> configureFromDropwizardBundles() {
            return option(ConfigureFromDropwizardBundles, true);
        }

        /**
         * Enables binding of interfaces implemented by configuration class to configuration instance
         * in guice context. Only interfaces directly implemented by any configuration class in configuration
         * classes hierarchy. Interfaces from java.* and groovy.*  packages are skipped.
         * This is useful to support {@code HasSomeConfiguration} interfaces convention.
         * <p>
         * When disabled, only classes in configuration hierarchy are registered (e.g. in case
         * {@code MyConfiguration extends MyBaseConfiguration extends Configuration}, all 3 classes would be bound.
         * <p>
         * Disabled by default.
         *
         * @return builder instance for chained calls
         * @see GuiceyOptions#BindConfigurationInterfaces
         */
        public Builder<T> bindConfigurationInterfaces() {
            return option(BindConfigurationInterfaces, true);
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
         * Print additional diagnostic logs with startup statistics, installed bundles, installers and resolved
         * extensions and configuration tree.
         * <p>
         * Statistics shows mainly where guice spent most time. Configuration info is
         * useful for configuration problems resolution.
         * Also, logs useful for better understanding how guicey works.
         * <p>
         * If custom logging format is required use {@link DiagnosticBundle} directly.
         * <p>
         * Bundle could be enabled indirectly with bundle lookup mechanism (e.g. with system property
         * {@code PropertyBundleLookup.enableBundles(DiagnosticBundle.class)}).
         * <p>
         * NOTE: Can't be used together with {@link #printAvailableInstallers()}.
         *
         * @return builder instance for chained calls
         * @see DiagnosticBundle
         */
        public Builder<T> printDiagnosticInfo() {
            bundle.context.registerBundles(new DiagnosticBundle());
            return this;
        }

        /**
         * Prints all registered (not disabled) installers with registration source. Useful to see all supported
         * extension types when multiple guicey bundles registered and available features become not obvious
         * from application class.
         * <p>
         * In contrast to {@link #printDiagnosticInfo()} shows all installers (including installers not used by
         * application extensions). Installers report intended only to show available installers and will not
         * show duplicate installers registrations or installers disabling (use diagnostic reporting for
         * all configuration aspects).
         * <p>
         * NOTE: Can't be used together with {@link #printDiagnosticInfo()}. Both serve different purposes:
         * available installers - to see what can be used and diagnostic info - to solve configuration problems
         * or better understand current configuration.
         *
         * @return builder instance for chained calls
         * @see DiagnosticBundle
         */
        public Builder<T> printAvailableInstallers() {
            bundle.context.registerBundles(
                    DiagnosticBundle.builder()
                            .printConfiguration(new DiagnosticConfig()
                                    .printInstallers()
                                    .printNotUsedInstallers())
                            .printContextTree(new ContextTreeConfig()
                                    .hideCommands()
                                    .hideDuplicateRegistrations()
                                    .hideEmptyBundles()
                                    .hideExtensions()
                                    .hideModules())
                            .build());
            return this;
        }

        /**
         * @param stage stage to run injector with
         * @return bundle instance
         * @see GuiceyOptions#InjectorStage
         */
        public GuiceBundle<T> build(final Stage stage) {
            option(InjectorStage, stage);
            return build();
        }

        /**
         * @return bundle instance with implicit PRODUCTION stage
         * @see GuiceyOptions#InjectorStage
         */
        public GuiceBundle<T> build() {
            return bundle;
        }
    }
}
