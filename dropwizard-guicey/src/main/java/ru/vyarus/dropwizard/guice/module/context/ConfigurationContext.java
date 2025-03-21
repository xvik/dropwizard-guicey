package ru.vyarus.dropwizard.guice.module.context;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.inject.Module;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.dropwizard.core.Configuration;
import io.dropwizard.core.ConfiguredBundle;
import io.dropwizard.core.cli.Command;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vyarus.dropwizard.guice.GuiceBundle;
import ru.vyarus.dropwizard.guice.GuiceyOptions;
import ru.vyarus.dropwizard.guice.bundle.GuiceyBundleLookup;
import ru.vyarus.dropwizard.guice.hook.ConfigurationHooksSupport;
import ru.vyarus.dropwizard.guice.hook.GuiceyConfigurationHook;
import ru.vyarus.dropwizard.guice.module.context.bootstrap.BootstrapProxyFactory;
import ru.vyarus.dropwizard.guice.module.context.bootstrap.DropwizardBundleTracker;
import ru.vyarus.dropwizard.guice.module.context.info.DropwizardBundleItemInfo;
import ru.vyarus.dropwizard.guice.module.context.info.GuiceyBundleItemInfo;
import ru.vyarus.dropwizard.guice.module.context.info.InstanceItemInfo;
import ru.vyarus.dropwizard.guice.module.context.info.ItemId;
import ru.vyarus.dropwizard.guice.module.context.info.ItemInfo;
import ru.vyarus.dropwizard.guice.module.context.info.ModuleItemInfo;
import ru.vyarus.dropwizard.guice.module.context.info.impl.ExtensionItemInfoImpl;
import ru.vyarus.dropwizard.guice.module.context.info.impl.GuiceyBundleItemInfoImpl;
import ru.vyarus.dropwizard.guice.module.context.info.impl.InstanceItemInfoImpl;
import ru.vyarus.dropwizard.guice.module.context.info.impl.ItemInfoImpl;
import ru.vyarus.dropwizard.guice.module.context.info.impl.ModuleItemInfoImpl;
import ru.vyarus.dropwizard.guice.module.context.info.sign.DisableSupport;
import ru.vyarus.dropwizard.guice.module.context.option.Option;
import ru.vyarus.dropwizard.guice.module.context.option.Options;
import ru.vyarus.dropwizard.guice.module.context.option.internal.OptionsSupport;
import ru.vyarus.dropwizard.guice.module.context.stat.Stat;
import ru.vyarus.dropwizard.guice.module.context.stat.StatTimer;
import ru.vyarus.dropwizard.guice.module.context.stat.StatsTracker;
import ru.vyarus.dropwizard.guice.module.context.unique.DuplicateConfigDetector;
import ru.vyarus.dropwizard.guice.module.installer.FeatureInstaller;
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBundle;
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyEnvironment;
import ru.vyarus.dropwizard.guice.module.installer.internal.ExtensionsHolder;
import ru.vyarus.dropwizard.guice.module.installer.scanner.ClasspathScanner;
import ru.vyarus.dropwizard.guice.module.lifecycle.internal.LifecycleSupport;
import ru.vyarus.dropwizard.guice.module.yaml.ConfigTreeBuilder;
import ru.vyarus.dropwizard.guice.module.yaml.ConfigurationTree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static ru.vyarus.dropwizard.guice.GuiceyOptions.BindConfigurationByPath;
import static ru.vyarus.dropwizard.guice.module.context.stat.Stat.BundleTime;
import static ru.vyarus.dropwizard.guice.module.context.stat.Stat.DropwizardBundleInitTime;

/**
 * Configuration context used internally to track all registered configuration items.
 * Items may be registered by type (installer, extension) or by instance (module, bundle).
 * <p>
 * Each item is registered only once, but all registrations are tracked. Uniqueness guaranteed by type.
 * <p>
 * Support generic disabling mechanism (for items marked with {@link DisableSupport} sign). If item is disabled, but
 * never registered special empty item info will be created at the end of configuration.
 * <p>
 * Considered as internal api.
 *
 * @author Vyacheslav Rusakov
 * @see ItemInfo for details of tracked info
 * @see ConfigurationInfo for acessing collected info at runtime
 * @since 06.07.2016
 */
@SuppressWarnings({"PMD.GodClass", "PMD.TooManyMethods", "checkstyle:ClassFanOutComplexity",
        "PMD.ExcessiveImports", "PMD.ExcessivePublicCount", "PMD.NcssCount", "PMD.CyclomaticComplexity",
        "PMD.TooManyFields", "PMD.ClassDataAbstractionCoupling", "checkstyle:ClassDataAbstractionCoupling"})
public final class ConfigurationContext {
    private final Logger logger = LoggerFactory.getLogger(ConfigurationContext.class);

    private final List<Predicate<Class<?>>> autoScanFilters = new ArrayList<>();
    private final List<DelayedConfig> delayedConfigurations = new ArrayList<>();
    private final SharedConfigurationState sharedState = new SharedConfigurationState();
    private DuplicateConfigDetector duplicates;
    private Bootstrap bootstrap;
    private Bootstrap bootstrapProxy;
    private Configuration configuration;
    private ConfigurationTree configurationTree;
    private Environment environment;
    private ExtensionsHolder extensionsHolder;
    private List<GuiceyBundle> initOrder;

    /**
     * Record executed hook classes.
     */
    private final List<Class<? extends GuiceyConfigurationHook>> hookTypes = new ArrayList<>();

    /**
     * Configured items (bundles, installers, extensions etc).
     * Preserve registration order.
     */
    private final Multimap<ConfigItem, Object> itemsHolder = LinkedHashMultimap.create();
    /**
     * Multiple instances of the same type could be registered for bundles or modules. Holds all registered
     * items by type to simplify duplicates detector calls.
     * Preserve registration order.
     */
    private final Multimap<Class<?>, Object> instanceItemsIndex = LinkedHashMultimap.create();
    /**
     * Configuration details (stored mostly for diagnostics).
     */
    private final Map<ItemId, ItemInfo> detailsHolder = Maps.newHashMap();
    /**
     * Holds disabled entries separately. Preserve registration order.
     */
    private final Multimap<ConfigItem, ItemId> disabledItemsHolder = LinkedHashMultimap.create();
    /**
     * Holds disable source for disabled items (item -- scope).
     */
    private final Multimap<ItemId, ItemId> disabledByHolder = LinkedHashMultimap.create();
    /**
     * Holds all instances considered as duplicates (and so ignored).
     */
    private final Multimap<ConfigItem, Object> duplicatesHolder = LinkedHashMultimap.create();
    /**
     * Holds used classes (extensions, modules etc) in order to detect same classes from different class loaders.
     */
    private final Map<String, Class<?>> usedClassesHolder = new HashMap<>();
    /**
     * Disable predicates listen for first item registration and could immediately disable it.
     */
    private final List<PredicateHandler> disablePredicates = new ArrayList<>();
    /**
     * Current scope hierarchy. The last one is actual scope (application or bundle).
     */
    private ItemId currentScope;

    /**
     * Used to gather guicey startup metrics.
     */
    private final StatsTracker tracker = new StatsTracker();
    /**
     * Used to set and get options within guicey.
     */
    private final OptionsSupport optionsSupport = new OptionsSupport();
    private final Options readOnlyOptions = new Options(optionsSupport);
    /**
     * Guicey lifecycle listeners support.
     */
    private final LifecycleSupport lifecycleTracker = new LifecycleSupport(tracker, readOnlyOptions,
            sharedState, tracker::verifyTimersDone);

    public ConfigurationContext() {
        // always available
        sharedState.put(Options.class, readOnlyOptions);
    }

    /**
     * Add extra filter for scanned classes (classpath scan and bindings recognition).
     *
     * @param filter filter instance
     */
    public void addAutoScanFilter(final Predicate<Class<?>> filter) {
        autoScanFilters.add(filter);
    }

    /**
     * @param clazz class to test
     * @return true if extension class could be used, false otherwise (auto scan filter)
     */
    public boolean isAcceptableAutoScanClass(final Class<?> clazz) {
        for (Predicate<Class<?>> filter : autoScanFilters) {
            if (!filter.test(clazz)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Change default duplicates detector.
     *
     * @param detector new policy
     */
    public void setDuplicatesDetector(final DuplicateConfigDetector detector) {
        if (duplicates != null) {
            logger.warn("Configured duplicates detector {} is overridden with {}",
                    duplicates.getClass().getSimpleName(), detector.getClass().getSimpleName());
        }
        this.duplicates = detector;
    }

    /**
     * Add delayed configuration from the main builder (or hook) to run under run phase (when configuration would be
     * available).
     *
     * @param config delayed configuration
     */
    public void addDelayedConfiguration(final Consumer<GuiceyEnvironment> config) {
        delayedConfigurations.add(new DelayedConfig(getScope(), config));
    }

    /**
     * Process delayed builder (or hooks) configurations.
     */
    public void processDelayedConfigurations(final GuiceyEnvironment environment) {
        for (DelayedConfig action : delayedConfigurations) {
            action.process(environment);
        }
    }

    // --------------------------------------------------------------------------- SCOPE

    /**
     * Current configuration context (application, bundle or classpath scan).
     *
     * @param scope scope key
     */
    public void openScope(final ItemId scope) {
        Preconditions.checkState(currentScope == null, "State error: current scope not closed");
        currentScope = scope;
    }

    /**
     * Declares possibly sub-configuration context. For example, to track dropwizard bundles initialization scope.
     *
     * @param scope scope key
     * @return previous scope or null (for application scope)
     */
    public ItemId replaceContextScope(final ItemId scope) {
        final ItemId current = currentScope;
        currentScope = scope;
        return current;
    }

    /**
     * Clears current scope.
     */
    public void closeScope() {
        Preconditions.checkState(currentScope != null, "State error: trying to close not opened scope");
        currentScope = null;
    }

    // --------------------------------------------------------------------------- COMMANDS

    /**
     * Register commands resolved with classpath scan.
     *
     * @param commands installed commands
     * @see ru.vyarus.dropwizard.guice.GuiceBundle.Builder#searchCommands()
     */
    public void registerCommands(final List<Class<Command>> commands) {
        openScope(ConfigScope.ClasspathScan.getKey());
        for (Class<Command> cmd : commands) {
            register(ConfigItem.Command, cmd);
        }
        closeScope();
    }

    // --------------------------------------------------------------------------- BUNDLES

    /**
     * Register bundles resolved by lookup mechanism. {@link GuiceyBundleLookup} used as context.
     *
     * @param bundles bundles resolved by lookup mechanism
     * @see GuiceyBundleLookup
     */
    public void registerLookupBundles(final List<GuiceyBundle> bundles) {
        openScope(ConfigScope.BundleLookup.getKey());
        for (GuiceyBundle bundle : bundles) {
            register(ConfigItem.Bundle, bundle);
        }
        closeScope();
        lifecycle().bundlesFromLookupResolved(bundles);
        lifecycle().bundlesResolved(getEnabledBundles(), getDisabledBundles(), getIgnoredItems(ConfigItem.Bundle));
    }

    /**
     * Usual bundle registration from {@link ru.vyarus.dropwizard.guice.GuiceBundle.Builder#bundles(GuiceyBundle...)}
     * or {@link ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBootstrap#bundles(GuiceyBundle...)}.
     * Context class is set to currently processed bundle.
     *
     * @param bundles bundles to register
     * @return list of actually registered bundles (without duplicates)
     */
    @SuppressWarnings("PMD.AvoidLiteralsInIfCondition")
    public List<GuiceyBundle> registerBundles(final GuiceyBundle... bundles) {
        final List<GuiceyBundle> res = new ArrayList<>();
        for (GuiceyBundle bundle : bundles) {
            final GuiceyBundleItemInfo info = register(ConfigItem.Bundle, bundle);
            // avoid duplicates (equal or same instances)
            if (info.getRegistrationAttempts() == 1) {
                res.add(bundle);
            }
        }
        return res;
    }

    /**
     * Guicey bundle manual disable registration from
     * {@link ru.vyarus.dropwizard.guice.GuiceBundle.Builder#disableBundles(Class[])}.
     *
     * @param bundles modules to disable
     */
    @SuppressWarnings("PMD.UseVarargs")
    public void disableBundle(final Class<? extends GuiceyBundle>[] bundles) {
        for (Class<? extends GuiceyBundle> bundle : bundles) {
            registerDisable(ConfigItem.Bundle, ItemId.from(bundle));
        }
    }

    /**
     * @return all configured bundles (without duplicates)
     */
    public List<GuiceyBundle> getEnabledBundles() {
        return getEnabledItems(ConfigItem.Bundle);
    }

    /**
     * Store initialization order of bundles. Due to transitive bundles immediate installation, real initialization
     * order could differ from registration order (because the root bundle would be registered before
     * a transitive bundle, but its processing would be finished after it).
     *
     * @param orderedBundles bundles in initialization order
     */
    public void storeBundlesInitOrder(final List<GuiceyBundle> orderedBundles) {
        initOrder = orderedBundles;
        int order = 1;
        for (GuiceyBundle bundle : orderedBundles) {
            ((GuiceyBundleItemInfoImpl) getInfo(bundle)).setInitOrder(order++);
        }
    }

    /**
     * It is important to call bundles run in the same order as bundles were initialized (it would
     * differ from registration order due to transitive bundles).
     *
     * @return all bundles in the initialization order
     */
    public List<GuiceyBundle> getBundlesOrdered() {
        return initOrder;
    }


    /**
     * Note: before configuration finalization this returns all actually disabled bundles and after
     * finalization all disables (including never registered bundles).
     *
     * @return all configured disabled bundles (without duplicates)
     */
    public List<GuiceyBundle> getDisabledBundles() {
        return getDisabledItems(ConfigItem.Bundle);
    }

    /**
     * Bundle must be disabled before it's processing, otherwise disabling will not have effect
     * (because bundle will be already processed and register all related items).
     *
     * @param id bundle id
     * @return true if bundle enabled, false otherwise
     */
    public boolean isBundleEnabled(final ItemId id) {
        return isEnabled(ConfigItem.Bundle, id);
    }

    // --------------------------------------------------------------------------- DROPWIZARD BUNDLES

    /**
     * Direct dropwizard bundle registration from
     * {@link ru.vyarus.dropwizard.guice.GuiceBundle.Builder#dropwizardBundles(ConfiguredBundle...)}
     * or {@link ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBootstrap#dropwizardBundles(
     * ConfiguredBundle[])}.
     * Context class is set to currently processed bundle.
     *
     * @param bundles dropwizard bundles
     */
    @SuppressWarnings("PMD.AvoidLiteralsInIfCondition")
    public void registerDropwizardBundles(final ConfiguredBundle... bundles) {
        for (ConfiguredBundle bundle : bundles) {
            final DropwizardBundleItemInfo info = register(ConfigItem.DropwizardBundle, bundle);
            // register only non duplicate bundles
            // bundles, registered in root GuiceBundle will be registered as soon as bootstrap would be available
            if (info.getRegistrationAttempts() == 1 && bootstrap != null) {
                installDropwizardBundle(bundle);
            }
        }
    }

    /**
     * Guicey bundle manual disable registration from
     * {@link ru.vyarus.dropwizard.guice.GuiceBundle.Builder#disableBundles(Class[])}.
     *
     * @param bundles modules to disable
     */
    @SuppressWarnings("PMD.UseVarargs")
    public void disableDropwizardBundle(final Class<? extends ConfiguredBundle>[] bundles) {
        for (Class<? extends ConfiguredBundle> bundle : bundles) {
            registerDisable(ConfigItem.DropwizardBundle, ItemId.from(bundle));
        }
    }

    /**
     * @return all configured dropwizard bundles (without duplicates)
     */
    public List<ConfiguredBundle> getEnabledDropwizardBundles() {
        return getEnabledItems(ConfigItem.DropwizardBundle);
    }

    /**
     * @return all disabled dropwizard bundles
     */
    public List<ConfiguredBundle> getDisabledDropwizardBundles() {
        return getDisabledItems(ConfigItem.DropwizardBundle);
    }

    /**
     * Proxy object created on first access because of ~200ms creation overhead.
     *
     * @return bootstrap proxy object
     */
    public Bootstrap getBootstrapProxy() {
        if (bootstrapProxy == null) {
            bootstrapProxy = BootstrapProxyFactory.create(bootstrap, this);
        }
        return bootstrapProxy;
    }

    // --------------------------------------------------------------------------- MODULES

    /**
     * @param modules guice modules to register
     */
    public void registerModules(final Module... modules) {
        for (Module module : modules) {
            register(ConfigItem.Module, module);
        }
    }

    /**
     * @param modules overriding guice modules to register
     */
    public void registerModulesOverride(final Module... modules) {
        ModuleItemInfoImpl.overrideScope(() -> {
            for (Module module : modules) {
                register(ConfigItem.Module, module);
            }
        });
    }

    /**
     * Guice module manual disable registration from
     * {@link ru.vyarus.dropwizard.guice.GuiceBundle.Builder#disableModules(Class[])}.
     *
     * @param modules modules to disable
     */
    @SuppressWarnings("PMD.UseVarargs")
    public void disableModules(final Class<? extends Module>[] modules) {
        for (Class<? extends Module> module : modules) {
            registerDisable(ConfigItem.Module, ItemId.from(module));
        }
    }

    /**
     * NOTE: both normal and overriding modules.
     *
     * @return all configured and enabled guice modules (without duplicates)
     */
    public List<Module> getEnabledModules() {
        return getEnabledItems(ConfigItem.Module);
    }

    /**
     * @return list of all enabled normal guice modules or empty list
     */
    public List<Module> getNormalModules() {
        return getEnabledModules().stream()
                .filter(mod -> !((ModuleItemInfo) getInfo(mod)).isOverriding())
                .collect(Collectors.toList());
    }

    /**
     * @return list of all enabled overriding modules or empty list
     */
    public List<Module> getOverridingModules() {
        return getEnabledModules().stream()
                .filter(mod -> ((ModuleItemInfo) getInfo(mod)).isOverriding())
                .collect(Collectors.toList());
    }

    /**
     * Note: before configuration finalization this returns all actually disabled modules and after
     * finalization all disables (including never registered modules).
     *
     * @return list of disabled modules or empty list
     */
    public List<Module> getDisabledModules() {
        return getDisabledItems(ConfigItem.Module);
    }

    /**
     * @return all disabled module types, including never registered
     */
    @SuppressWarnings("unchecked")
    public List<Class<Object>> getDisabledModuleTypes() {
        final Collection disabledIds = disabledItemsHolder.get(ConfigItem.Module);
        return ItemId.typesOnly((Collection<ItemId<Object>>) disabledIds);
    }

    // --------------------------------------------------------------------------- INSTALLERS

    /**
     * Usual installer registration from {@link ru.vyarus.dropwizard.guice.GuiceBundle.Builder#installers(Class[])}
     * or {@link ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBootstrap#installers(Class[])}.
     *
     * @param installers installers to register
     */
    @SuppressWarnings("PMD.UseVarargs")
    public void registerInstallers(final Class<? extends FeatureInstaller>[] installers) {
        for (Class<? extends FeatureInstaller> installer : installers) {
            register(ConfigItem.Installer, installer);
        }
    }

    /**
     * Register installers from classpath scan. Use {@link ClasspathScanner} as context class.
     *
     * @param installers installers found by classpath scan
     */
    public void registerInstallersFromScan(final List<Class<? extends FeatureInstaller>> installers) {
        openScope(ConfigScope.ClasspathScan.getKey());
        for (Class<? extends FeatureInstaller> installer : installers) {
            register(ConfigItem.Installer, installer);
        }
        closeScope();
    }

    /**
     * Installer manual disable registration from
     * {@link ru.vyarus.dropwizard.guice.GuiceBundle.Builder#disableInstallers(Class[])}
     * or {@link ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBootstrap#disableInstallers(Class[])}.
     *
     * @param installers installers to disable
     */
    @SuppressWarnings("PMD.UseVarargs")
    public void disableInstallers(final Class<? extends FeatureInstaller>[] installers) {
        for (Class<? extends FeatureInstaller> installer : installers) {
            registerDisable(ConfigItem.Installer, ItemId.from(installer));
        }
    }

    /**
     * @return all configured and enabled installers (including resolved by scan)
     */
    public List<Class<? extends FeatureInstaller>> getEnabledInstallers() {
        return getEnabledItems(ConfigItem.Installer);
    }

    /**
     * Note: before configuration finalization this returns all actually disabled installers and after
     * finalization all disables (including never registered installers).
     *
     * @return list of disabled installers
     */
    public List<Class<? extends FeatureInstaller>> getDisabledInstallers() {
        return getDisabledItems(ConfigItem.Installer);
    }

    /**
     * Called with prepared list of installers to use for extensions recognition and installation.
     *
     * @param installers installers to use in correct order
     */
    public void installersResolved(final List<FeatureInstaller> installers) {
        this.extensionsHolder = new ExtensionsHolder(installers);
        lifecycle().installersResolved(new ArrayList<>(installers), getDisabledInstallers());
    }

    // --------------------------------------------------------------------------- EXTENSIONS


    /**
     * @return extensions holder object
     */
    public ExtensionsHolder getExtensionsHolder() {
        return extensionsHolder;
    }

    /**
     * Usual extension registration from {@link ru.vyarus.dropwizard.guice.GuiceBundle.Builder#extensions(Class[])}
     * or {@link ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBootstrap#extensions(Class[])}.
     *
     * @param extensions extensions to register
     */
    public void registerExtensions(final Class<?>... extensions) {
        for (Class<?> extension : extensions) {
            register(ConfigItem.Extension, extension);
        }
    }

    /**
     * Optional extension registration from
     * {@link ru.vyarus.dropwizard.guice.GuiceBundle.Builder#extensionsOptional(Class[])} or
     * {@link ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBootstrap#extensionsOptional(Class[])}.
     *
     * @param extensions optional extensions to register
     */
    public void registerExtensionsOptional(final Class<?>... extensions) {
        for (Class<?> extension : extensions) {
            final ExtensionItemInfoImpl info = register(ConfigItem.Extension, extension);
            info.setOptional(true);
        }
    }

    /**
     * Extensions classpath scan requires testing with all installers to recognize actual extensions.
     * To avoid duplicate installers recognition, extensions resolved by classpath scan are registered
     * immediately. It's required because of not obvious method used for both manually registered extensions
     * (to obtain container) and to create container from extensions from classpath scan.
     *
     * @param extension found extension
     * @param fromScan  true when called for extension found in classpath scan, false for manually
     *                  registered extension
     * @return extension info container
     */
    public ExtensionItemInfoImpl getOrRegisterExtension(final Class<?> extension, final boolean fromScan) {
        final ExtensionItemInfoImpl info;
        if (fromScan) {
            openScope(ConfigScope.ClasspathScan.getKey());
            info = register(ConfigItem.Extension, extension);
            closeScope();
        } else {
            // info will be available for sure because such type was stored before (during manual registration)
            info = getInfo(extension);
        }

        return info;
    }

    /**
     * Registration of extension detected from guice binding. Descriptor for extension may already exists.
     * <p>
     * Top guice module must be used because it's the only module, known by guicey configuration model and so
     * the only way to show it properly on configuration report. Guice report can show extensions in correct
     * positions, if required.
     *
     * @param extension extension class
     * @param topScope  top module in registration modules hierarchy
     * @return extension info container
     */
    public ExtensionItemInfoImpl getOrRegisterBindingExtension(final Class<?> extension,
                                                               final Class<? extends Module> topScope) {
        openScope(ItemId.from(topScope));
        // extension could be already registered by classpath scan or manually and in this case just registration
        // attempt will be registered
        final ExtensionItemInfoImpl info = register(ConfigItem.Extension, extension);
        closeScope();
        return info;
    }

    /**
     * Extension recognized by installer, which means it is now completely initialized and disable predicates
     * could be applied now.
     *
     * @param extension extension after setting installer
     */
    public void notifyExtensionRecognized(final ExtensionItemInfoImpl extension) {
        Preconditions.checkState(extension.isAllDataCollected());
        fireRegistration(extension, true);
    }

    /**
     * Extension manual disable registration from
     * {@link ru.vyarus.dropwizard.guice.GuiceBundle.Builder#disableExtensions(Class[])}.
     *
     * @param extensions extensions to disable
     */
    @SuppressWarnings("PMD.UseVarargs")
    public void disableExtensions(final Class<?>[] extensions) {
        for (Class<?> extension : extensions) {
            registerDisable(ConfigItem.Extension, ItemId.from(extension));
        }
    }

    /**
     * @param extension extension type
     * @return true if extension is enabled, false if disabled
     */
    public boolean isExtensionEnabled(final Class<?> extension) {
        return isEnabled(ConfigItem.Extension, ItemId.from(extension));
    }

    /**
     * @return all configured extensions (including resolved by scan)
     */
    public List<Class<?>> getEnabledExtensions() {
        return getEnabledItems(ConfigItem.Extension);
    }

    /**
     * Note: before configuration finalization this returns all actually disabled extensions and after
     * finalization all disables (including never registered extensions).
     *
     * @return list of disabled extensions
     */
    public List<Class<?>> getDisabledExtensions() {
        return getDisabledItems(ConfigItem.Extension);
    }

    // --------------------------------------------------------------------------- OPTIONS

    /**
     * @param option option enum
     * @param value  option value (not null)
     * @param <T>    helper type to define option
     */
    @SuppressWarnings("unchecked")
    public <T extends Enum & Option> void setOption(final T option, final Object value) {
        optionsSupport.set(option, value);
    }

    /**
     * @param option option enum
     * @param <V>    value type
     * @param <T>    helper type to define option
     * @return option value (set or default)
     */
    @SuppressWarnings("unchecked")
    public <V, T extends Enum & Option> V option(final T option) {
        return (V) optionsSupport.get(option);
    }

    /**
     * @return options support object
     */
    public OptionsSupport options() {
        return optionsSupport;
    }

    /**
     * @return read-only options
     */
    public Options optionsReadOnly() {
        return readOnlyOptions;
    }

    // --------------------------------------------------------------------------- GENERAL

    /**
     * Register disable predicates, used to disable all matched items.
     * <p>
     * After registration predicates are applied to all currently registered items to avoid registration
     * order influence.
     *
     * @param predicates disable predicates
     */
    @SuppressWarnings({"PMD.UseVarargs", "unchecked"})
    public void registerDisablePredicates(final Predicate<? extends ItemInfo>[] predicates) {
        // accept typed predicates, but downgrade to base entity (assumed that predicate would be formed
        // correctly with Disables.* methods)
        final List<PredicateHandler> list = Arrays.stream(predicates)
                .map(p -> new PredicateHandler((Predicate<ItemInfo>) p, getScope()))
                .collect(Collectors.toList());
        disablePredicates.addAll(list);
        applyPredicatesForRegisteredItems(list);
    }

    /**
     * Runs all registered hooks (at the end of manual builder configuration) and fire success event.
     *
     * @param builder bundle builder
     */
    public void runHooks(final GuiceBundle.Builder builder) {
        final StatTimer timer = stat().timer(Stat.HooksTime);
        ConfigurationHooksSupport.logRegisteredAliases();
        // lookup hooks from system property "guicey.hooks" (lookup executed after builder configuration to
        // let user declare alias hooks)
        ConfigurationHooksSupport.loadSystemHooks();
        // Support for external configuration (for tests)
        // Use special scope to distinguish external configuration
        openScope(ConfigScope.Hook.getKey());
        final Set<GuiceyConfigurationHook> hooks = ConfigurationHooksSupport.run(builder, stat());
        hooks.forEach(hook -> hookTypes.add(hook.getClass()));
        closeScope();
        timer.stop();
        lifecycle().configurationHooksProcessed(hooks);
    }

    /**
     * @param bootstrap dropwizard bootstrap instance
     */
    public void initPhaseStarted(final Bootstrap bootstrap) {
        this.bootstrap = bootstrap;
        // register in shared state just in case
        this.sharedState.put(Bootstrap.class, bootstrap);
        this.sharedState.assignTo(bootstrap.getApplication());
        lifecycle().beforeInit(bootstrap);
        // delayed init of registered dropwizard bundles
        final StatTimer time = stat().timer(BundleTime);
        final StatTimer dwtime = stat().timer(DropwizardBundleInitTime);
        for (ConfiguredBundle bundle : getEnabledDropwizardBundles()) {
            installDropwizardBundle(bundle);
        }
        dwtime.stop();
        time.stop();
        lifecycle().dropwizardBundlesInitialized(getEnabledDropwizardBundles(),
                getDisabledDropwizardBundles(), getIgnoredItems(ConfigItem.DropwizardBundle));
    }

    /**
     * @param configuration dropwizard configuration instance
     * @param environment   dropwizard environment instance
     */
    public void runPhaseStarted(final Configuration configuration, final Environment environment) {
        this.configuration = configuration;
        final StatTimer timer = stat().timer(Stat.ConfigurationAnalysis);
        this.configurationTree = ConfigTreeBuilder
                .build(bootstrap, configuration, option(BindConfigurationByPath));
        timer.stop();
        this.environment = environment;
        // register in shared state just in case
        this.sharedState.put(Configuration.class, configuration);
        this.sharedState.put(ConfigurationTree.class, configurationTree);
        this.sharedState.put(Environment.class, environment);
        this.sharedState.listen(environment);
        lifecycle().runPhase(configuration, configurationTree, environment);
    }

    /**
     * Called when context configuration is finished (but extensions installation is not finished yet).
     * Merges disabled items configuration with registered items or creates new items to hold disable info.
     */
    public void finalizeConfiguration() {
        // process disabled items
        for (ConfigItem type : disabledItemsHolder.keys()) {
            for (ItemId item : disabledItemsHolder.get(type)) {
                final Collection<Object> instances = instanceItemsIndex.get(item.getType());
                if (type.isInstanceConfig() && !instances.isEmpty()) {
                    // disable all instances
                    for (Object instance : instances) {
                        final DisableSupport info = getOrCreateInfo(type, instance);
                        info.getDisabledBy().addAll(disabledByHolder.get(item));
                    }
                } else {
                    DisableSupport info = (DisableSupport) detailsHolder.get(item);
                    if (info == null) {
                        // if no registrations, create empty disable-only item (for instance items by type only)
                        info = getOrCreateInfo(type, item.getType());
                    }

                    info.getDisabledBy().addAll(disabledByHolder.get(item));
                }
            }
        }

        // prepare extensions for installation
        final List<ExtensionItemInfoImpl> exts = new ArrayList<>();
        for (Class<?> ext : getEnabledExtensions()) {
            exts.add(getInfo(ext));
        }
        extensionsHolder.registerExtensions(exts);
    }

    /**
     * Called after complete guice bundle startup (other dropwizard bundle's run phase (for bundles registered
     * after guice bundle) may not yet be called).
     */
    public void bundleStarted() {
        lifecycle().applicationRun();
        sharedState.forgetStartupInstance();
    }

    /**
     * @param type config type
     * @param <T>  expected items type
     * @return list of all registered items of type or empty list
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> getItems(final ConfigItem type) {
        final Collection<Object> res = itemsHolder.get(type);
        return res.isEmpty() ? Collections.<T>emptyList() : (List<T>) Lists.newArrayList(res);
    }

    /**
     * @param type   config type
     * @param filter items filter
     * @param <T>    expected items type
     * @return list of all items matching filter or empty list
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> getItems(final ConfigItem type, final Predicate<T> filter) {
        final Collection<T> items = (Collection<T>) itemsHolder.get(type);
        if (items.isEmpty()) {
            return Collections.emptyList();
        }
        return items.stream().filter(filter).collect(Collectors.toList());
    }

    /**
     * @param type config type
     * @param <T>  expected items type
     * @return list of all detected duplicates of this configuration type
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> getIgnoredItems(final ConfigItem type) {
        final Collection<Object> res = duplicatesHolder.get(type);
        return res.isEmpty() ? Collections.<T>emptyList() : (List<T>) Lists.newArrayList(res);
    }

    /**
     * @param item item to get info
     * @param <T>  expected container type
     * @return item registration info container or null if item not registered
     */
    @SuppressWarnings("unchecked")
    public <T extends ItemInfoImpl> T getInfo(final Object item) {
        return (T) detailsHolder.get(ItemId.from(item));
    }

    /**
     * @return startup statistics tracker instance
     */
    public StatsTracker stat() {
        return tracker;
    }

    /**
     * @return lifecycle events broadcaster
     */
    public LifecycleSupport lifecycle() {
        return lifecycleTracker;
    }

    /**
     * @return dropwizard bootstrap object
     */
    public Bootstrap getBootstrap() {
        return bootstrap;
    }

    /**
     * @return dropwizard configuration object
     */
    public Configuration getConfiguration() {
        return configuration;
    }

    /**
     * @return introspected configuration object
     */
    public ConfigurationTree getConfigurationTree() {
        return configurationTree;
    }

    /**
     * @return dropwizard environment object
     */
    public Environment getEnvironment() {
        return environment;
    }

    /**
     * @return application-wide registry object
     */
    public SharedConfigurationState getSharedState() {
        return sharedState;
    }

    /**
     * @return types of executed hooks
     */
    public List<Class<? extends GuiceyConfigurationHook>> getExecutedHookTypes() {
        return hookTypes;
    }

    private ItemId getScope() {
        return currentScope == null ? ConfigScope.Application.getKey() : currentScope;
    }

    /**
     * Disable predicate could be registered after some items registration and to make sure that predicate
     * affects all these items - apply to all currenlty registered items.
     *
     * @param predicates new predicates
     */
    private void applyPredicatesForRegisteredItems(final List<PredicateHandler> predicates) {
        ImmutableList.builder()
                .addAll(getEnabledModules())
                .addAll(getEnabledBundles())
                .addAll(getEnabledExtensions())
                .addAll(getEnabledInstallers())
                .build()
                .stream()
                .<ItemInfo>map(this::getInfo)
                // avoid extensions without installer set (could be important for disable)
                .filter(ItemInfo::isAllDataCollected)
                .forEach(item -> applyDisablePredicates(predicates, item));
    }

    private void registerDisable(final ConfigItem type, final ItemId id) {
        if (type.isInstanceConfig() && id.getIdentity() == null && disabledByHolder.containsKey(id)) {
            // when disabling by class, remove all per instance records (only one type record should remain)
            // yes, it will loose some info about exact instance, but it doesn't matter anymore as entire type disabled
            disabledItemsHolder.get(type).removeIf(it -> it.getIdentity() != null && it.equals(id));
            final Iterator<Map.Entry<ItemId, ItemId>> iterator = disabledByHolder.entries().iterator();
            while (iterator.hasNext()) {
                final Map.Entry<ItemId, ItemId> entry = iterator.next();
                final ItemId key = entry.getKey();
                if (key.getIdentity() != null && key.equals(id)) {
                    iterator.remove();
                }
            }
        }
        // multimaps will filter duplicates automatically
        disabledItemsHolder.put(type, id);
        disabledByHolder.put(id, getScope());
    }

    private <T extends ItemInfoImpl> T register(final ConfigItem type, final Object srcItem) {
        final Object item = detectDuplicate(type, srcItem);
        final T info = getOrCreateInfo(type, item);

        if (type.isInstanceConfig() && info.getRegistrationAttempts() == 0) {
            // mark registration order for instance items to differentiate them
            // (but only for actually registered - duplicate instances are ignored)
            ((InstanceItemInfoImpl) info).setInstanceCount(instanceItemsIndex.get(info.getType()).size());
        }

        // if registered multiple times in one scope attempts will reveal it
        info.countRegistrationAttempt(getScope());

        fireRegistration(info, false);
        return info;
    }

    /**
     * For instance types, registered instance may be a duplicate (relative to already registered).
     * It may be either obvious duplicate when the same instance is already registered or
     * duplicate by some other meaning (by default if objects are equal). If duplicate is detected then
     * object must not be used for new configuration item creation, instead it must be tracked as another
     * registration attempt to already registered item.
     * <p>
     * Instances of the same class, but loaded with different class loaders must be properly checked for deduplication
     * in equals method (for example, {@link ru.vyarus.dropwizard.guice.module.context.unique.item.UniqueGuiceyBundle}
     * use class name instead of class itself for correct handling of multi class loaders case).
     * <p>
     * For class types, registered class is checked by name in order to detect already registered extension, but
     * from different class loader. In case of detection, original class will be used instead of provied to prevent
     * duplicate extension loading.
     *
     * @param type item type
     * @param item item instance
     * @return correct item to use for registration
     */
    @SuppressFBWarnings("NP_NULL_PARAM_DEREF")
    private Object detectDuplicate(final ConfigItem type, final Object item) {
        Object original = null;
        if (type.isInstanceConfig()) {
            final Class clsFromOtherCL = detectClassFromDifferentClassLoader(item.getClass());
            // use correct class in order to correctly detect registered instances of the same type..
            // this way instances could rely only on properly implemented equals (or external deduplicator)
            final Collection<Object> registeredInstances = instanceItemsIndex
                    .get(clsFromOtherCL != null ? clsFromOtherCL : item.getClass());
            // when at least one instance of the same type registered - check for duplicates
            if (!registeredInstances.isEmpty()) {
                original = findDuplicateInstance(type, registeredInstances, item);
                if (clsFromOtherCL != null && original == null) {
                    // important to warn in case of incomplete equals implementation (for cases when it's not intended)
                    logger.warn("Registered instances of class {} use different class loaders and may not be "
                                    + "properly checked for duplicates: {}, {}",
                            clsFromOtherCL.getName(), clsFromOtherCL.getClassLoader().toString(),
                            item.getClass().getClassLoader().toString());
                    // NOTE in configuration items class will not be unified, so care must be taken when building
                    // reports (or perform other configuration analysis)
                }
            }
            if (original == null) {
                // register item as accepted
                instanceItemsIndex.put(item.getClass(), item);
            }
        } else {
            final Class clsFromOtherCL = detectClassFromDifferentClassLoader((Class) item);
            if (clsFromOtherCL != null) {
                // use already registered class to avoid duplicate extensions registrations
                // (from different classloaders)
                original = clsFromOtherCL;
            }
        }
        return MoreObjects.firstNonNull(original, item);
    }

    /**
     * Checks if class with the same name was already registered, but from different class loader. This is important
     * for extensions deduplication because installers reporting will not reveal duplicate classes (they usually hide
     * duplicates appeared from guice reportings).
     *
     * @param cls class to check
     * @return already registered class from different class loader or null
     */
    private Class detectClassFromDifferentClassLoader(final Class cls) {
        final String clsName = cls.getName();
        final Class reg = usedClassesHolder.get(clsName);

        // detected already registered class, but loaded by different class loader
        if (reg != null && !reg.equals(cls)) {
            return reg;
        } else {
            // remember class for future comparisons
            usedClassesHolder.put(clsName, cls);
        }
        return null;
    }

    @SuppressWarnings({"unchecked", "PMD.CompareObjectsWithEquals"})
    private Object findDuplicateInstance(final ConfigItem type,
                                         final Collection<Object> registeredInstances,
                                         final Object item) {
        Object original = null;
        // first check if exactly the same instance already registered or object is equal to some
        // already registered instance - automatic duplicate
        for (Object reg : registeredInstances) {
            // do this instead of simple contains to cover cases when hashcode is not implemented properly
            if (reg == item || reg.equals(item)) {
                original = reg;
                break;
            }
        }
        if (original == null && duplicates != null) {
            // use external duplicate detection logic
            original = duplicates.getDuplicateItem(new ArrayList<>(registeredInstances), item);
            if (original != null) {
                Preconditions.checkState(registeredInstances.contains(original),
                        "Incorrect instance duplicates detection result: returned item must "
                                + "be already registered");
            }
        }
        if (original != null && original != item) {
            final ItemId<Object> originalId = ItemId.from(original);
            final InstanceItemInfo originalInfo = (InstanceItemInfo) detailsHolder.get(originalId);
            // remember duplicate item, but lost instance itself
            originalInfo.getDuplicates().add(ItemId.from(item));

            // log as info because duplicate instances may be way not obvious from code
            logger.info("IGNORE {} {}/{} as duplicate for {}/{} (#{})",
                    type, getScope().getType().getSimpleName(), ItemId.from(item),
                    originalInfo.getRegistrationScope().getType().getSimpleName(),
                    originalId, originalInfo.getInstanceCount());
            duplicatesHolder.put(type, item);
        }
        return original;
    }

    private void fireRegistration(final ItemInfo item, final boolean afterCompleteInitialization) {
        // Fire event only for initial registration and for items which could be disabled
        // Apply predicate ONLY when all required data prepared (affects extensions)
        if (item instanceof DisableSupport && item.isAllDataCollected()
                && (item.getRegistrationAttempts() == 1 || afterCompleteInitialization)) {
            applyDisablePredicates(disablePredicates, item);
        }
    }

    private void applyDisablePredicates(final List<PredicateHandler> predicates, final ItemInfo item) {
        for (PredicateHandler predicate : predicates) {
            if (predicate.disable(item)) {
                break;
            }
        }
    }

    /**
     * Disabled items may not be actually registered. In order to register disable info in uniform way
     * dummy container is created.
     *
     * @param type item type
     * @param item item object
     * @param <T>  expected container type
     * @return info container instance
     */
    @SuppressWarnings("unchecked")
    private <T extends ItemInfoImpl> T getOrCreateInfo(final ConfigItem type, final Object item) {
        final ItemId id = ItemId.from(item);
        final T info;
        // details holder allows to implicitly filter by type and avoid duplicate registration
        // for instance type it will detect exactly the same instance (same object)
        if (detailsHolder.containsKey(id)) {
            // no duplicate registration
            info = (T) detailsHolder.get(id);
        } else {
            // initial registration
            itemsHolder.put(type, item);
            info = type.newContainer(item);
            detailsHolder.put(id, info);
        }
        return info;
    }

    private <T> List<T> getDisabledItems(final ConfigItem type) {
        final Collection<ItemId> disabled = disabledItemsHolder.get(type);
        return disabled.isEmpty()
                ? Collections.emptyList() : getItems(type, item -> disabled.contains(ItemId.from(item)));
    }

    private <T> List<T> getEnabledItems(final ConfigItem type) {
        final Collection<ItemId> disabled = disabledItemsHolder.get(type);
        return disabled.isEmpty()
                ? getItems(type) : getItems(type, item -> !disabled.contains(ItemId.from(item)));
    }

    private boolean isEnabled(final ConfigItem type, final ItemId itemId) {
        return !disabledItemsHolder.get(type).contains(itemId);
    }

    @SuppressWarnings("unchecked")
    private void installDropwizardBundle(final ConfiguredBundle bundle) {
        // register decorated dropwizard bundle to track transitive bundles
        // or bundle directly if tracking disabled
        bootstrap.addBundle(option(GuiceyOptions.TrackDropwizardBundles)
                ? new DropwizardBundleTracker(bundle, this) : bundle);
    }

    /**
     * Wraps registered disable predicate on registration to remember it's scope and mark all actually disabled
     * items as disabled by that scope.
     */
    private class PredicateHandler {
        private final ItemId predicateScope;
        private final Predicate<ItemInfo> predicate;

        PredicateHandler(final Predicate<ItemInfo> predicate, final ItemId predicateScope) {
            this.predicate = predicate;
            this.predicateScope = predicateScope;
        }

        /**
         * Checks if item matches predicate and if so disables item. Item disable source will be set to
         * predicate registration source.
         *
         * @param item item to check for disabling
         * @return true if item was disabled, false otherwise
         */
        public boolean disable(final ItemInfo item) {
            final boolean test = predicate.test(item);
            if (test) {
                final ItemId scope = currentScope;
                // change scope to indicate predicate's registration scope as disable source
                currentScope = predicateScope;
                registerDisable(item.getItemType(), item.getId());
                currentScope = scope;
            }
            return test;
        }
    }

    /**
     * Delayed configuration action with preserved source context.
     */
    private class DelayedConfig {
        private final ItemId scope;
        private final Consumer<GuiceyEnvironment> action;

        DelayedConfig(final ItemId scope, final Consumer<GuiceyEnvironment> action) {
            this.scope = scope;
            this.action = action;
        }

        public void process(final GuiceyEnvironment environment) {
            final ItemId original = currentScope;
            // change scope to indicate actual registration scope (diff. app scope and hook)
            currentScope = scope;
            action.accept(environment);
            currentScope = original;
        }
    }
}
