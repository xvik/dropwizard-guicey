package ru.vyarus.dropwizard.guice;


import com.google.common.base.Preconditions;
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
import ru.vyarus.dropwizard.guice.debug.*;
import ru.vyarus.dropwizard.guice.debug.hook.DiagnosticHook;
import ru.vyarus.dropwizard.guice.debug.report.diagnostic.DiagnosticConfig;
import ru.vyarus.dropwizard.guice.debug.report.guice.GuiceAopConfig;
import ru.vyarus.dropwizard.guice.debug.report.guice.GuiceConfig;
import ru.vyarus.dropwizard.guice.debug.report.tree.ContextTreeConfig;
import ru.vyarus.dropwizard.guice.debug.report.web.MappingsConfig;
import ru.vyarus.dropwizard.guice.debug.report.yaml.BindingsConfig;
import ru.vyarus.dropwizard.guice.hook.ConfigurationHooksSupport;
import ru.vyarus.dropwizard.guice.hook.GuiceyConfigurationHook;
import ru.vyarus.dropwizard.guice.injector.DefaultInjectorFactory;
import ru.vyarus.dropwizard.guice.injector.InjectorFactory;
import ru.vyarus.dropwizard.guice.module.GuiceyInitializer;
import ru.vyarus.dropwizard.guice.module.GuiceyRunner;
import ru.vyarus.dropwizard.guice.module.context.ConfigurationContext;
import ru.vyarus.dropwizard.guice.module.context.SharedConfigurationState;
import ru.vyarus.dropwizard.guice.module.context.info.ItemInfo;
import ru.vyarus.dropwizard.guice.module.context.option.Option;
import ru.vyarus.dropwizard.guice.module.context.unique.DuplicateConfigDetector;
import ru.vyarus.dropwizard.guice.module.context.unique.UniqueItemsDuplicatesDetector;
import ru.vyarus.dropwizard.guice.module.installer.CoreInstallersBundle;
import ru.vyarus.dropwizard.guice.module.installer.FeatureInstaller;
import ru.vyarus.dropwizard.guice.module.installer.InstallersOptions;
import ru.vyarus.dropwizard.guice.module.installer.WebInstallersBundle;
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBundle;
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyEnvironment;
import ru.vyarus.dropwizard.guice.module.installer.internal.CommandSupport;
import ru.vyarus.dropwizard.guice.module.jersey.debug.HK2DebugBundle;
import ru.vyarus.dropwizard.guice.module.lifecycle.GuiceyLifecycleListener;
import ru.vyarus.dropwizard.guice.module.support.ConfigurationAwareModule;

import javax.servlet.DispatcherType;
import java.util.EnumSet;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static ru.vyarus.dropwizard.guice.GuiceyOptions.*;
import static ru.vyarus.dropwizard.guice.module.installer.InstallersOptions.JerseyExtensionsManagedByGuice;

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
 * @author Vyacheslav Rusakov
 * @see ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo for configuratio diagnostic
 * @since 31.08.2014
 */
@SuppressWarnings({
        "PMD.ExcessiveClassLength", "PMD.ExcessiveImports", "PMD.TooManyMethods", "PMD.ExcessivePublicCount"})
public final class GuiceBundle implements ConfiguredBundle<Configuration> {

    private final ConfigurationContext context = new ConfigurationContext();
    private InjectorFactory injectorFactory = new DefaultInjectorFactory();
    private GuiceyBundleLookup bundleLookup = new DefaultBundleLookup();

    GuiceBundle() {
        // Bundle should be instantiated only from builder
    }

    @Override
    public void initialize(final Bootstrap bootstrap) {
        // perform classpath scan if required, register dropwizard bundles
        final GuiceyInitializer starter = new GuiceyInitializer(bootstrap, context);

        // resolve and init all guicey bundles
        starter.initializeBundles(bundleLookup);
        // scan for commands (if enabled)
        starter.findCommands();
        // scan for installers (if scan enabled) and installers initialization
        starter.resolveInstallers();
        // scan for extensions (if scan enabled) and validation of all registered extensions
        starter.resolveExtensions();

        starter.initFinished();
    }

    @Override
    public void run(final Configuration configuration, final Environment environment) throws Exception {
        // deep configuration parsing (config paths resolution)
        final GuiceyRunner runner = new GuiceyRunner(context, configuration, environment);

        // process guicey bundles
        runner.runBundles();
        // prepare guice modules for injector creation
        runner.prepareModules();
        // create injector
        runner.createInjector(injectorFactory,
                runner.analyzeAndRepackageBindings());
        // install extensions by instance
        runner.installExtensions();
        // inject command fields
        runner.injectCommands();

        runner.runFinished();
    }

    /**
     * Note that injector could be accessed statically anywhere with
     * {@link ru.vyarus.dropwizard.guice.injector.lookup.InjectorLookup#getInjector(io.dropwizard.Application)}.
     *
     * @return created injector instance or fail if injector not yet created
     * @throws IllegalStateException if injector is not yet created
     */
    public Injector getInjector() {
        // InjectorLookup not used because it requires application instance, which may not be available yet
        return context.getSharedState().getOrFail(Injector.class, "Guice not initialized");
    }

    /**
     * @return builder instance to construct bundle
     */
    public static Builder builder() {
        return new Builder()
                // allow enabling diagnostic logs with system property (on compiled app): -Dguicey.hooks=diagnostic
                .hookAlias("diagnostic", DiagnosticHook.class);
    }

    /**
     * Builder encapsulates bundle configuration options.
     */
    @SuppressWarnings({"checkstyle:ClassDataAbstractionCoupling", "checkstyle:ClassFanOutComplexity"})
    public static class Builder {
        private final GuiceBundle bundle = new GuiceBundle();

        /**
         * Guicey broadcast a lot of events in order to indicate lifecycle phases
         * ({@link ru.vyarus.dropwizard.guice.module.lifecycle.GuiceyLifecycle}). This could be useful
         * for diagnostic logging (like {@link #printLifecyclePhases()}) or to implement special
         * behaviours on installers, bundles, modules extensions (listeners have access to everything).
         * For example, {@link ConfigurationAwareModule} like support for guice modules could be implemented
         * with listeners.
         * <p>
         * Configuration items (modules, extensions, bundles) are not aware of each other and listeners
         * could be used to tie them. For example, to tell bundle if some other bundles registered (limited
         * applicability, but just for example).
         * <p>
         * You can also use {@link ru.vyarus.dropwizard.guice.module.lifecycle.GuiceyLifecycleAdapter} when you need to
         * handle multiple events (it replaces direct events handling with simple methods).
         * <p>
         * Listener is not registered if equal listener were already registered ({@link java.util.Set} used as
         * listeners storage), so if you need to be sure that only one instance of some listener will be used
         * implement {@link Object#equals(Object)} and {@link Object#hashCode()}.
         *
         * @param listeners guicey lifecycle listeners
         * @return builder instance for chained calls
         * @see ru.vyarus.dropwizard.guice.module.lifecycle.GuiceyLifecycle
         * @see ru.vyarus.dropwizard.guice.module.lifecycle.GuiceyLifecycleAdapter
         * @see ru.vyarus.dropwizard.guice.module.lifecycle.UniqueGuiceyLifecycleListener
         */
        public Builder listen(final GuiceyLifecycleListener... listeners) {
            bundle.context.lifecycle().register(listeners);
            return this;
        }

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
        public <K extends Enum & Option> Builder option(final K option, final Object value) {
            bundle.context.setOption(option, value);
            return this;
        }

        /**
         * Sets multiple options at once. Method would be useful for options lookup implementations.
         * <p>
         * Note: {@link Option} type is not mixed with enum in declaration to simplify usage,
         * but used enums must be correct options.
         * <p>
         * Use provided {@link ru.vyarus.dropwizard.guice.module.context.option.mapper.OptionsMapper}
         * utility to map options from system properties or environment variables. It supports basic string conversions.
         * For example:
         * <pre>{@code
         *     .options(new OptionsMapper()
         *                 .env("STAGE", GuiceyOptions.InjectorStage)
         *                 .map())
         * }</pre>
         * Will set injector stage option from STAGE environment variable. If variable is not set - default value used.
         * If STAGE set to, for example "DEVELOPMENT" (must be Stage enum value) then Stage.DEVELOPMENT will be
         * set as option value.
         * <p>
         * Also, mapper could map generic options definitions from system properties (prefixed):
         * <pre>{@code
         * .options(new OptionsMapper()
         *                 .props("option.")
         *                 .map())
         * }</pre>
         * See {@link ru.vyarus.dropwizard.guice.module.context.option.mapper.OptionsMapper} for more usage details.
         *
         * @param options options map (not null)
         * @param <K>     helper type for option signature definition
         * @return builder instance for chained calls
         * @throws NullPointerException     is null value provided for any option
         * @throws IllegalArgumentException if any provided value incompatible with option type
         * @see #option(Enum, Object) for more info
         * @see ru.vyarus.dropwizard.guice.module.context.option.mapper.OptionsMapper
         */
        @SuppressWarnings("unchecked")
        public <K extends Enum & Option> Builder options(final Map<Enum, Object> options) {
            ((Map<K, Object>) (Map) options).forEach(this::option);
            return this;
        }

        /**
         * Configures custom {@link InjectorFactory}. Required by some guice extensions like governator.
         *
         * @param injectorFactory custom guice injector factory
         * @return builder instance for chained calls
         */
        public Builder injectorFactory(final InjectorFactory injectorFactory) {
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
         * @see #duplicateConfigDetector(DuplicateConfigDetector)
         */
        public Builder bundleLookup(final GuiceyBundleLookup bundleLookup) {
            bundle.bundleLookup = bundleLookup;
            return this;
        }

        /**
         * Disables default bundle lookup.
         *
         * @return builder instance for chained calls
         */
        public Builder disableBundleLookup() {
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
        public Builder enableAutoConfig(final String... basePackages) {
            Preconditions.checkState(basePackages.length > 0, "Specify at least one package to scan");
            return option(ScanPackages, basePackages);
        }

        /**
         * Duplicate configuration detector decides what configuration items, registered as instance (guicey bundle,
         * guice module) to consider duplicate (and so avoid duplicates installation). By default, multiple instances
         * of the same type allowed (the same as with dropwizard bundles - you can register multiple instances). But
         * same instances or equal ({@link Object#equals(Object)}) instances are considered duplicate. If you need to
         * accept only one instance of bundle or module, simply implement equals method to make all instances equal.
         * Custom deduplicatation implementation may be required for 3rd party instances, where proper equals
         * implementation is impossible (or for more complicated duplicates detection logic).
         * <p>
         * Example situation: suppose one common bundle (or guice module) is used by two other bundles, so
         * it would be registered in two bundles, but, if these bundles would be used together, then two instances of
         * common bundle would be registered, which is often not desired. To workaround such case, bundle must
         * implement proper equals method or custom duplication detector implementation must be used.
         * <p>
         * Use {@link ru.vyarus.dropwizard.guice.module.context.unique.LegacyModeDuplicatesDetector} to simulate
         * legacy guicey behaviour when only one instance of type is allowed (if old behaviour is important).
         * Use {@link #uniqueItems(Class[])} to grant uniqueness for some items only.
         *
         * @param detector detector implementation
         * @return builder instance for chained calls
         */
        public Builder duplicateConfigDetector(final DuplicateConfigDetector detector) {
            bundle.context.setDuplicatesDetector(detector);
            return this;
        }

        /**
         * Grant uniqueness for specified instance items: guicey bundles, guice modules and dropwizard bundles,
         * registered through guicey api ({@link #dropwizardBundles(ConfiguredBundle[])} and
         * {@link GuiceyOptions#TrackDropwizardBundles}). That means that if multiple instances
         * of specified type would be registered, only one instance will actually be used (and other would be
         * considered as duplicate configurations).
         * <p>
         * Method actually registers custom detector implementation
         * {@link #duplicateConfigDetector(DuplicateConfigDetector)} and so can't be used together with other
         * custom duplicates detector.
         * <p>
         * Warning: in contrast to other builder methods, configurations from multiple method calls are not
         * aggregated (each new call to overrides previous configuration), so specify all unique items in one call.
         *
         * @param configurationItems instance configuration items (bundles or modules) to grant uniqueness
         * @return builder instance for chained calls
         */
        public Builder uniqueItems(final Class<?>... configurationItems) {
            return duplicateConfigDetector(new UniqueItemsDuplicatesDetector(configurationItems));
        }

        /**
         * Multiple module instances of the same type could be registered. If module uniqueness is important
         * use {@link ru.vyarus.dropwizard.guice.module.context.unique.item.UniqueModule} with correct
         * equals implementation or implement custom deduplication logic in
         * {@link #duplicateConfigDetector(DuplicateConfigDetector)}.
         * <p>
         * These modules are registered under initialization phase where you don't have access for configuration
         * or environment objects. To workaround this you can use *AwareModule interfaces, or extend from
         * {@link ru.vyarus.dropwizard.guice.module.support.DropwizardAwareModule} and required objects will be set
         * just before configuration start. Another option is to register module inside
         * {@link GuiceyBundle#run(GuiceyEnvironment)}, which is called under run phase.
         *
         * @param modules one or more guice modules
         * @return builder instance for chained calls
         * @see ru.vyarus.dropwizard.guice.module.support.BootstrapAwareModule
         * @see ru.vyarus.dropwizard.guice.module.support.ConfigurationAwareModule
         * @see ru.vyarus.dropwizard.guice.module.support.EnvironmentAwareModule
         * @see ru.vyarus.dropwizard.guice.module.support.OptionsAwareModule
         * @see ru.vyarus.dropwizard.guice.module.support.DropwizardAwareModule
         */
        public Builder modules(final Module... modules) {
            Preconditions.checkState(modules.length > 0, "Specify at least one module");
            bundle.context.registerModules(modules);
            return this;
        }

        /**
         * Register overriding modules, used to override bindings of normal modules. Internally guice
         * {@link com.google.inject.util.Modules#override(Module...)} is used. If binding is present in normal module
         * and overriding module then overriding module binding is used. Bindings in overriding modules, not present
         * in normal modules are simply added to context (as usual module).
         * <p>
         * The main purpose for overriding modules is testing, but it also could be used (in rare cases) to
         * amend existing guice context (for example, in case when you do not control all bindings, but need
         * to hot fix something).
         * <p>
         * Overriding modules behave the same as normal modules: they are inspected for *AwareModule interfaces
         * to inject dropwizard objects. Overriding module could be disabled with {@link #disableModules(Class[])} or
         * generic {@link #disable(Predicate[])}.
         *
         * @param modules overriding modules
         * @return builder instance for chained calls
         * @see #modules(Module...)
         * @see ru.vyarus.dropwizard.guice.test.binding.BindingsOverrideInjectorFactory to override overridden
         * bindings in test (edge case(
         */
        public Builder modulesOverride(final Module... modules) {
            bundle.context.registerModulesOverride(modules);
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
        public Builder searchCommands() {
            return option(SearchCommands, true);
        }

        /**
         * Disables automatic {@link CoreInstallersBundle} and {@link WebInstallersBundle} registration
         * (no installers will be registered by default).
         *
         * @return builder instance for chained calls
         * @see GuiceyOptions#UseCoreInstallers
         */
        public Builder noDefaultInstallers() {
            return option(UseCoreInstallers, false);
        }

        /**
         * Disables {@link com.google.inject.servlet.GuiceFilter} registration for both application and admin contexts.
         * Without guice filter registered, guice {@link com.google.inject.servlet.ServletModule} registrations
         * are useless (because they can't be used). So guice servlet modules support will be disabled:
         * no request or session scopes may be used and registrations of servlet modules will be denied
         * (binding already declared exception). Even with disabled guice filter, request and response
         * objects provider injections still may be used in resources (will work through HK2 provider).
         * <p>
         * Guice servlets initialization took ~50ms, so injector creation will be a bit faster after disabling.
         *
         * @return builder instance for chained calls
         * @see GuiceyOptions#GuiceFilterRegistration
         * @deprecated in the next version HK2 support will be removed and guice request scope will be mandatory
         */
        @Deprecated
        public Builder noGuiceFilter() {
            return option(GuiceFilterRegistration, EnumSet.noneOf(DispatcherType.class));
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
        public final Builder installers(final Class<? extends FeatureInstaller>... installers) {
            bundle.context.registerInstallers(installers);
            return this;
        }

        /**
         * Beans could be registered automatically when auto scan enabled,
         * but if you don't want to use it, you can register beans manually.
         * <p>
         * Guice injector will instantiate beans and registered installers will be used to recognize and properly
         * register provided extension beans. Startup will fail if bean not recognized by installers.
         * <p>
         * Also, could be used to add beans from packages not included in auto scanning.
         * <p>
         * Alternatively, you can manually bind extensions in guice module and they would be recognized
         * ({@link GuiceyOptions#AnalyzeGuiceModules}).
         *
         * @param extensionClasses extension bean classes to register
         * @return builder instance for chained calls
         */
        public Builder extensions(final Class<?>... extensionClasses) {
            bundle.context.registerExtensions(extensionClasses);
            return this;
        }

        /**
         * The same as {@link #extensions(Class[])}, but, in case if no installer recognize extension, will be
         * automatically disabled instead of throwing error. Useful for optional extensions declaration, which
         * must be activated only when some 3rd party bundle appear. For example, it could be some diagnostic
         * info provider, which must be activated when 3rd party diagnostic bundle is enabled (via bundles lookup
         * or with hook).
         * <p>
         * Alternatively, you can manually bind extensions in guice module and they would be recognized
         * ({@link GuiceyOptions#AnalyzeGuiceModules}). Extensions with no available target installer will simply
         * wouldn't be detected (because installers used for recognition) and so there is no need
         * to mark them as optional in this case.
         *
         * @param extensionClasses extension bean classes to register
         * @return builder instance for chained calls
         */
        public Builder extensionsOptional(final Class<?>... extensionClasses) {
            bundle.context.registerExtensionsOptional(extensionClasses);
            return this;
        }

        /**
         * Guicey bundles are mainly useful for extensions (to group installers and extensions installation without
         * auto scan). Bundles lifecycle is the same as dropwizard bundles and so it could be used together.
         * <p>
         * Multiple bundle instances of the same type could be registered. If bundle uniqueness is important
         * use {@link ru.vyarus.dropwizard.guice.module.context.unique.item.UniqueGuiceyBundle} with correct
         * equals implementation or implement custom deduplication logic in
         * {@link #duplicateConfigDetector(DuplicateConfigDetector)}.
         *
         * @param bundles guicey bundles
         * @return builder instance for chained calls
         */
        public Builder bundles(final GuiceyBundle... bundles) {
            bundle.context.registerBundles(bundles);
            return this;
        }

        /**
         * Dropwizard bundles registration. There is no difference with direct bundles registration (with
         * {@link Bootstrap#addBundle(ConfiguredBundle)}, which is actually called almost immediately),
         * except guicey tracking (registered bundles are showed in diagnostic report), ability to disable bundle and
         * automatic duplicates detection (see {@link #duplicateConfigDetector(DuplicateConfigDetector)}).
         * <p>
         * Only bundles registered with guicey api are checked! For example, if you register one instance of the same
         * bundle directly into bootstrap and another one with guicey api - guicey will not be able to track duplicate
         * registration. So it's better to register all bundles through guicey api.
         * <p>
         * By default, guicey will also track transitive bundles registrations (when registered bundle register
         * other bundles) so disable and deduplication logic will apply to them too. Tracking could be disabled
         * with {@link GuiceyOptions#TrackDropwizardBundles} option.
         *
         * @param bundles guicey bundles
         * @return builder instance for chained calls
         */
        public Builder dropwizardBundles(final ConfiguredBundle... bundles) {
            bundle.context.registerDropwizardBundles(bundles);
            return this;
        }

        /**
         * Disabling installer will lead to avoiding all relative installed extensions. If you have manually
         * registered extensions for disabled installer then remove their registration. Classpath scan extensions
         * will be ignored (not installed, because no installer to recognize them).
         * <p>
         * Disabling installer may be used to replace some existing installer with modified version (probably fixed):
         * disable existing installer and register new custom installer.
         *
         * @param installers feature installer types to disable
         * @return builder instance for chained calls
         */
        @SafeVarargs
        public final Builder disableInstallers(final Class<? extends FeatureInstaller>... installers) {
            bundle.context.disableInstallers(installers);
            return this;
        }

        /**
         * Extensions disable is mostly useful for testing. In some cases, it could be used to disable some extra
         * extensions installed with classpath scan or bundle. But, generally, try to avoid manual extensions
         * disabling for clearer application configuration.
         *
         * @param extensions extensions to disable (manually added, registered by bundles or with classpath scan)
         * @return builder instance for chained calls
         */
        public final Builder disableExtensions(final Class<?>... extensions) {
            bundle.context.disableExtensions(extensions);
            return this;
        }

        /**
         * Modules disable is mostly useful for testing. In some cases, it could be used to disable some extra
         * modules installed by some bundle. But, generally, try to avoid manual modules disabling for
         * clearer application configuration.
         * <p>
         * Option could also disable inner modules (registered by modules transitively), but only if bindings analysis
         * is not disabled (by {@link GuiceyOptions#AnalyzeGuiceModules}). Inner modules can't be removed from
         * overriding modules, because only normal modules are analyzed.
         * <p>
         * When bindings analysis is disabled this option can disable only directly registered modules
         * (with {@link #modules(Module...)} or in bundle
         * {@link ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBootstrap#modules(Module...)}.
         *
         * @param modules guice module types to disable
         * @return builder instance for chained calls
         */
        @SafeVarargs
        public final Builder disableModules(final Class<? extends Module>... modules) {
            bundle.context.disableModules(modules);
            return this;
        }

        /**
         * Bundles disable is mostly useful for testing. In some cases, it could be used to disable some transitive
         * bundle (bundle installed by some registered bundle with
         * {@link ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBootstrap#bundles(GuiceyBundle...)}).
         *
         * @param bundles guicey bundles to disable
         * @return builder instance for chained calls
         */
        @SafeVarargs
        public final Builder disableBundles(final Class<? extends GuiceyBundle>... bundles) {
            bundle.context.disableBundle(bundles);
            return this;
        }

        /**
         * Dropwizard bundles disable is mostly useful for testing. Note that it can disable only bundles directly
         * registered through guicey api or bundles registered by them (if dropwizard bundles tracking
         * not disabled with {@link GuiceyOptions#TrackDropwizardBundles}).
         *
         * @param bundles guicey bundles to disable
         * @return builder instance for chained calls
         */
        @SafeVarargs
        public final Builder disableDropwizardBundles(final Class<? extends ConfiguredBundle>... bundles) {
            bundle.context.disableDropwizardBundle(bundles);
            return this;
        }

        /**
         * Disable items using disable predicate: all matched items will be disabled. Predicate is called
         * just after first item registration and, if it will evaluate to true, then item marked as disabled.
         * Predicate receive only disableable items: guicey bundle, installer, extension or guice module
         * (directly registered).
         * <p>
         * Also, predicate is called on registration for all already registered items to make predicate
         * registration moment not important.
         * <p>
         * Essentially, predicates are the same as calling direct disable methods: items, disabled by predicate,
         * will be marked as disabled by predicate registration context (application or guicey bundle).
         * <p>
         * Mostly useful for testing, but in some cases could be used directly.
         * <p>
         * Use {@link Predicate#and(Predicate)}, {@link Predicate#or(Predicate)} and {@link Predicate#negate()}
         * to combine complex predicates from simple ones from
         * {@link ru.vyarus.dropwizard.guice.module.context.Disables} helper.
         * <p>
         * Item passed only one time after initial registration and so item object will have only basic fields:
         * item type, item class and item registration scope (who register it).
         * <p>
         * For example, disable all installers, registered from application root
         * except SomeInstallerType:
         * <pre><code>
         * import static ru.vyarus.dropwizard.guice.module.context.Disables.*
         *
         * builder.disable(installer()
         *             .and(registeredBy(Application.class))
         *             .and(type(SomeInstallerType.class).negate());
         * </code></pre>
         * <p>
         * Items could be disabled just by class, no matter what type they are:
         * <pre><code>
         * import static ru.vyarus.dropwizard.guice.module.context.Disables.*
         *
         * builder.disable(type(MyExtension.class,
         *                      MyInstaller.class,
         *                      MyBundle.class,
         *                      MyModule.class));
         * </code></pre>
         * <p>
         * For instance types (bundles, modules), when multiple instances of the same class may appear,
         * exact instance could be disabled (predicate would be called for each new instance separately,
         * avoiding equal duplicates).
         *
         * @param predicates disable predicates
         * @return builder instance for chained calls
         * @see ru.vyarus.dropwizard.guice.module.context.Disables for common predicates
         */
        @SafeVarargs
        public final Builder disable(final Predicate<ItemInfo>... predicates) {
            bundle.context.registerDisablePredicates(predicates);
            return this;
        }

        /**
         * Enables strict control of beans instantiation context: all beans must be instantiated by guice, except
         * beans annotated with {@link ru.vyarus.dropwizard.guice.module.installer.feature.jersey.JerseyManaged}.
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
         * @deprecated in the next version HK2 support will be removed and option will become useless
         */
        @Deprecated
        public Builder strictScopeControl() {
            bundle.context.registerBundles(new HK2DebugBundle());
            return this;
        }

        /**
         * Manage jersey extensions (resources, jersey filters etc.) with HK2 by default instead of guice (the
         * same effect as if {@link ru.vyarus.dropwizard.guice.module.installer.feature.jersey.JerseyManaged} annotation
         * would be set on all beans). Use this option if you want to use jersey specific resource features
         * (like @Context bindings) and completely delegate management to HK2.
         * <p>
         * IMPORTANT: this will activate HK2 bridge usage (to be able to inject guice beans) and so you will need
         * to provide bridge dependency (org.glassfish.hk2:guice-bridge:2.6.1). Startup will fail if
         * dependency is not available.
         * <p>
         * WARNING: you will not be able to use guice AOP on beans managed by HK2!
         *
         * @return builder instance for chained calls
         * @see InstallersOptions#JerseyExtensionsManagedByGuice
         * @see ru.vyarus.dropwizard.guice.module.installer.feature.jersey.JerseyManaged
         * @see ru.vyarus.dropwizard.guice.module.installer.feature.jersey.GuiceManaged
         * @deprecated in the next version HK2 support will be removed
         */
        @Deprecated
        public Builder useHK2ForJerseyExtensions() {
            option(JerseyExtensionsManagedByGuice, false);
            option(UseHkBridge, true);
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
         * If custom logging format is required use {@link ConfigurationDiagnostic} directly.
         * <p>
         * May be enabled on compiled application with a system property: {@code -Dguicey.hooks=diagnostic}
         * (hook will also enable some other reports).
         *
         * @return builder instance for chained calls
         * @see ConfigurationDiagnostic
         */
        public Builder printDiagnosticInfo() {
            return listen(new ConfigurationDiagnostic());
        }

        /**
         * Prints extensions usage help: all extension signs recognized by installers. Installers printed in
         * execution order.
         * <p>
         * Not that custom installers must provide this information by overriding
         * {@link ru.vyarus.dropwizard.guice.module.installer.FeatureInstaller#getRecognizableSigns()}.
         * 
         * @return builder instance for chained calls
         */
        public Builder printExtensionsHelp() {
            return listen(new ExtensionsHelpDiagnostic());
        }

        /**
         * Prints all registered (not disabled) installers with registration source. Useful to see all supported
         * extension types when multiple guicey bundles registered and available features become not obvious
         * from application class.
         * <p>
         * In contrast to {@link #printDiagnosticInfo()} shows all installers (including installers not used by
         * application extensions). Installers report intended only to show available installers and will not
         * show duplicate installers registrations or installers disabling (use diagnostic reporting for
         * all configuration aspects). Also, report will indicate installers marker interfaces and so it will
         * be obvious what installer did: install by type or by object, perform custom guice bindings or perform
         * jersey specific installations.
         *
         * @return builder instance for chained calls
         * @see ConfigurationDiagnostic
         */
        public Builder printAvailableInstallers() {
            return listen(ConfigurationDiagnostic.builder("Available installers report")
                    .printConfiguration(new DiagnosticConfig()
                            .printInstallers()
                            .printNotUsedInstallers()
                            .printInstallerInterfaceMarkers())
                    .printContextTree(new ContextTreeConfig()
                            .hideCommands()
                            .hideDuplicateRegistrations()
                            .hideEmptyBundles()
                            .hideExtensions()
                            .hideModules())
                    .build());
        }

        /**
         * Prints available configuration bindings. Use it to see available bindings or debug missed bindings
         * as report shown before actual injector start. Use {@link #printCustomConfigurationBindings()} to see only
         * custom paths (without dropwizard configurations).
         * <p>
         * Safe to use with other print* options.
         * <p>
         * Info: guicey analyze configuration object (prepared by dropwizard) with jackson serialization api
         * and binds all readable configuration values by path (e.g. {@code @inject @Config("some.path") String prop;}.
         * Also, unique sub configuration objects are recognized and may be used directly
         * ({@code @Inject @Config SubConfig subConf}). Introspected configuration object is accessible from
         * lifecycle events, gucie modules, guicey bundles and by direct injection.
         * <p>
         * May be enabled on compiled application with a system property: {@code -Dguicey.hooks=diagnostic}
         * (hook will also enable some other reports).
         *
         * @return builder instance for chained calls
         * @see ru.vyarus.dropwizard.guice.module.yaml.ConfigurationTree
         * @see ru.vyarus.dropwizard.guice.module.yaml.bind.Config
         */
        public Builder printConfigurationBindings() {
            return listen(new YamlBindingsDiagnostic());
        }

        /**
         * The same as {@link #printConfigurationBindings()}, but hides all dropwizard related paths.
         * Use to see bindings of your custom configuration classes.
         *
         * @return builder instance for chained calls
         */
        public Builder printCustomConfigurationBindings() {
            return listen(new YamlBindingsDiagnostic(
                    new BindingsConfig()
                            .showConfigurationTree()
                            .showNullValues()
                            .showCustomConfigOnly()));
        }

        /**
         * Prints guice bindings configured in user-provided modules (excluding guicey and guice own bindings).
         * Identifies bindings overrides from overriding modules, aop and undeclared (JIT) bindings.
         * <p>
         * May be enabled on compiled application with a system property: {@code -Dguicey.hooks=diagnostic}
         * (hook will also enable some other reports).
         *
         * @return builder instance for chained calls
         * @see #printAllGuiceBindings() to show entire injector state
         */
        public Builder printGuiceBindings() {
            return listen(new GuiceBindingsDiagnostic(new GuiceConfig()
                    .hideGuiceBindings()
                    .hideGuiceyBindings()));
        }

        /**
         * Prints all bindings in guice injector.
         *
         * @return builder instance for chained calls
         * @see #printGuiceBindings() to show only user-provided bindings
         */
        public Builder printAllGuiceBindings() {
            return listen(new GuiceBindingsDiagnostic(new GuiceConfig()));
        }

        /**
         * Prints all configured guice AOP interceptors and how they apply to methods.
         * In most cases this general report will be not useful as it will contain too much information. It's
         * better to use {@link #printGuiceAopMap(GuiceAopConfig)} and filter all non interesting bindings.
         *
         * @return builder instance for chained calls
         */
        public Builder printGuiceAopMap() {
            return printGuiceAopMap(new GuiceAopConfig());
        }

        /**
         * Supposed to be used to understand how guice AOP works (the most often reasons: check aop applied to method
         * and check interceptors order). In contrast to other reports, this one is more a specialized tool for
         * development.
         * <p>
         * Multiple reports could be configured (in some cases it is simpler to declare few different but simple
         * configurations rather then one complex).
         *
         * @param config report configuration
         * @return builder instance for chained calls
         */
        public Builder printGuiceAopMap(final GuiceAopConfig config) {
            return listen(new GuiceAopDiagnostic(config));
        }

        /**
         * Split logs with major lifecycle stage names. Useful for debugging (to better understand
         * at what stage your code is executed). Also, could be used for light profiling as
         * time since startup is printed for each phase (and for shutdown phases). And, of course,
         * could be used for better guicey understanding.
         *
         * @return builder instance for chained calls
         * @see LifecycleDiagnostic
         */
        public Builder printLifecyclePhases() {
            return listen(new LifecycleDiagnostic(false));
        }

        /**
         * Same as {@link #printLifecyclePhases}, but also prints resolved and disabled configuration items.
         * <p>
         * May be enabled on compiled application with a system property: {@code -Dguicey.hooks=diagnostic}
         * (hook will also enable some other reports).
         *
         * @return builder instance for chained calls
         * @see LifecycleDiagnostic
         */
        public Builder printLifecyclePhasesDetailed() {
            return listen(new LifecycleDiagnostic(true));
        }

        /**
         * Prints all configured filters and servlets (including guice {@link com.google.inject.servlet.ServletModule}
         * declarations.
         * <p>
         * May be enabled on compiled application with a system property: {@code -Dguicey.hooks=diagnostic}
         * (hook will also enable some other reports).
         *
         * @return builder instance for chained calls
         */
        public Builder printWebMappings() {
            return listen(new WebMappingsDiagnostic(new MappingsConfig()
                    .showMainContext()
                    .showAdminContext()
                    .showDropwizardMappings()
                    .showGuiceMappings()));
        }

        /**
         * Prints all registered jersey extensions (including core dropwizard extensions and everything
         * registered by other dropwizard bundles or manually).
         * <p>
         * May be enabled on compiled application with a system property: {@code -Dguicey.hooks=diagnostic}
         * (hook will also enable some other reports).
         *
         * @return builder instance for chained calls
         */
        public Builder printJerseyConfig() {
            return listen(new JerseyConfigDiagnostic());
        }

        /**
         * Guicey hooks ({@link GuiceyConfigurationHook}) may be loaded with system property "guicey.hooks". But
         * it may be not comfortable to always declare full class name (e.g. -Dguicey.hooks=com.foo.bar.Hook,..).
         * Instead short alias name may be used: -Dguicey.hooks=alias1, alias2.
         * <p>
         * By default, diagnostic hook is aliased as "diagnostic" in order to be able to enable diagnostic reporting
         * for compiled application ({@code -Dguicey.hooks=diagnostic}).
         *
         * @param name alias name
         * @param hook hook class to alias
         * @return builder instance for chained calls
         */
        public Builder hookAlias(final String name, final Class<? extends GuiceyConfigurationHook> hook) {
            ConfigurationHooksSupport.registerSystemHookAlias(name, hook);
            return this;
        }

        /**
         * Guicey manage application-wide shared state object to simplify cases when such state is required during
         * configuration. It may be used by bundles or hooks to "communicate". For example, server pages bundles
         * use this to unify global configuration. Unified place intended to replace all separate "hacks" and
         * so simplify testing. Shared application state could be access statically anywhere during application
         * life.
         * <p>
         * Caution: this is intended to be used only in cases when there is no other option except global state.
         * <p>
         * This method could be useful for possible hook needы (maybe hooks communications) because there is no
         * other way to access shared state by hooks (bundles may use special api or reference by application instance)
         *
         * @param stateAction state action
         * @return builder instance for chained calls
         */
        public Builder withSharedState(final Consumer<SharedConfigurationState> stateAction) {
            stateAction.accept(bundle.context.getSharedState());
            return this;
        }

        /**
         * @param stage stage to run injector with
         * @return bundle instance
         * @see GuiceyOptions#InjectorStage
         */
        public GuiceBundle build(final Stage stage) {
            option(InjectorStage, stage);
            return build();
        }

        /**
         * @return bundle instance with implicit PRODUCTION stage
         * @see GuiceyOptions#InjectorStage
         */
        public GuiceBundle build() {
            bundle.context.runHooks(this);
            return bundle;
        }
    }
}
