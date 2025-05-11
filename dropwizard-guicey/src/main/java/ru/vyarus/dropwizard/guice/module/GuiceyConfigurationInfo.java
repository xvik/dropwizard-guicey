package ru.vyarus.dropwizard.guice.module;

import com.google.common.collect.Sets;
import com.google.inject.Module;
import io.dropwizard.core.ConfiguredBundle;
import io.dropwizard.core.cli.Command;
import ru.vyarus.dropwizard.guice.module.context.ConfigItem;
import ru.vyarus.dropwizard.guice.module.context.ConfigScope;
import ru.vyarus.dropwizard.guice.module.context.ConfigurationInfo;
import ru.vyarus.dropwizard.guice.module.context.Filters;
import ru.vyarus.dropwizard.guice.module.context.info.ExtensionItemInfo;
import ru.vyarus.dropwizard.guice.module.context.info.GuiceyBundleItemInfo;
import ru.vyarus.dropwizard.guice.module.context.info.ItemId;
import ru.vyarus.dropwizard.guice.module.context.info.ItemInfo;
import ru.vyarus.dropwizard.guice.module.context.info.ModuleItemInfo;
import ru.vyarus.dropwizard.guice.module.context.info.sign.DisableSupport;
import ru.vyarus.dropwizard.guice.module.context.option.OptionsInfo;
import ru.vyarus.dropwizard.guice.module.context.stat.StatsInfo;
import ru.vyarus.dropwizard.guice.module.installer.FeatureInstaller;
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBundle;
import ru.vyarus.dropwizard.guice.module.installer.internal.ExtensionsHolder;
import ru.vyarus.dropwizard.guice.module.yaml.ConfigurationTree;

import javax.inject.Inject;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static ru.vyarus.dropwizard.guice.module.context.info.ItemId.typesOnly;

/**
 * Public api for internal guicey configuration info and startup statistics. Provides information about time spent
 * for configurations and configuration details like registered bundle types,
 * installers, extensions, disabled installers etc. Registered as guice bean and could be directly injected.
 * <p>
 * Could be used for configuration diagnostics or unit test checks.
 *
 * @author Vyacheslav Rusakov
 * @since 21.06.2016
 */
public class GuiceyConfigurationInfo {

    private final ConfigurationInfo context;
    private final StatsInfo stats;
    private final OptionsInfo options;
    private final ExtensionsHolder holder;
    private final ConfigurationTree configurationTree;

    /**
     * Create configuration info.
     *
     * @param context           configuration context
     * @param stats             stat trackers
     * @param options           options
     * @param holder            extensions holder
     * @param configurationTree parsed configuration
     */
    @Inject
    public GuiceyConfigurationInfo(final ConfigurationInfo context, final StatsInfo stats,
                                   final OptionsInfo options, final ExtensionsHolder holder,
                                   final ConfigurationTree configurationTree) {
        this.context = context;
        this.stats = stats;
        this.options = options;
        this.holder = holder;
        this.configurationTree = configurationTree;
    }

    /**
     * Use to perform custom configuration items data lookups (e.g. for additional logging,
     * diagnostics or consistency checks).
     *
     * @return raw configuration info object
     */
    public ConfigurationInfo getData() {
        return context;
    }

    /**
     * Timers and counters collected at startup.
     *
     * @return startup statistics object
     * @see ru.vyarus.dropwizard.guice.module.context.stat.Stat for available stats
     */
    public StatsInfo getStats() {
        return stats;
    }

    /**
     * @return configuration options
     * @see ru.vyarus.dropwizard.guice.module.context.option.Option for more info
     * @see ru.vyarus.dropwizard.guice.GuiceyOptions for options example
     */
    public OptionsInfo getOptions() {
        return options;
    }

    /**
     * Note that object is also available for direct injection because, in contrast to other guicey-related
     * data, configuration properties tree could be used directly in business logic.
     *
     * @return dropwizard configuration introspection result
     */
    public ConfigurationTree getConfigurationTree() {
        return configurationTree;
    }

    /**
     * Shortcut for {@link #getItemsByScope(Class)} for special scopes (like classpath scan, bundles lookup etc).
     *
     * @param specialScope special scope
     * @return all enabled items registered in specified scope or empty list
     */
    public List<ItemId<Object>> getItemsByScope(final ConfigScope specialScope) {
        return getItemsByScope(specialScope.getKey());
    }

    /**
     * NOTE: single item may be registered from multiple scopes! This method will return entity by all it's registered
     * scopes and not just by first registration. It makes it usable, for example, for configuration tree building.
     * If you need exact registration scope use {@link Filters#registrationScope(Class)} filter.
     *
     * @param scope required scope
     * @return all enabled items registered in specified scope or empty list
     * @see ItemInfo#getRegisteredBy() for more info about scopes
     * @see ConfigScope for the list of all special scopes
     */
    public List<ItemId<Object>> getItemsByScope(final ItemId scope) {
        return context.getItems(Filters.enabled().and(Filters.registeredBy(scope)));
    }

    /**
     * Shortcut for {@link #getItemsByScope(ItemId)}. Note that bundle of the same type may be registered multiple
     * times and querying by such bundle type will return registrations from all bundle instances.
     *
     * @param scope scope class
     * @return all enabled items registered in specified scope or empty list
     */
    public List<ItemId<Object>> getItemsByScope(final Class<?> scope) {
        return getItemsByScope(ItemId.from(scope));
    }

    /**
     * @return all active scopes including disable only scopes
     * @see #getActiveScopes(boolean)
     * @see ConfigScope for the list of all special scopes
     */
    public Set<ItemId> getActiveScopes() {
        return getActiveScopes(true);
    }

    /**
     * @param countDisables include scopes with disables only
     * @return all active scopes or empty collection
     * @see ItemInfo#getRegisteredBy() for more info about scopes
     */
    public Set<ItemId> getActiveScopes(final boolean countDisables) {
        final Set<ItemId> res = Sets.newHashSet();
        context.getItems(it -> {
            res.addAll(it.getRegisteredBy());
            if (countDisables && it instanceof DisableSupport) {
                res.addAll(((DisableSupport) it).getDisabledBy());
            }
            return true;
        });
        return res;
    }

    /**
     * Shortcut for {@link ConfigurationInfo#getInfos(Class)} which always returns one configuration info
     * object per class. It is generally not correct for instance types (where multiple instances of the same
     * type could be registered), but enough for general reporting needs (showing general info on class level).
     *
     * @param type item type
     * @param <T>  required info object type
     * @return configuration item object for provided class (first one for multiple instances) or empty list if not
     * found
     */
    public <T extends ItemInfo> T getInfo(final Class<?> type) {
        final List<T> res = getInfos(type);
        return res.isEmpty() ? null : res.get(0);
    }

    // NOTE shortcut for getting by ItemId not provided to avoid easy mistakes by obtaining info by class
    // and loosing instances in case of multiple instane of the same type registration
    // use getData().getInfo() instead

    /**
     * Shortcut for {@link ConfigurationInfo#getInfos(Class)}. Added to avoid confusion with {@link #getInfo(Class)}.
     *
     * @param type item type
     * @param <T>  required info object type
     * @return all configuration object registered with required class (could be multiple for instance types
     * and always one element for class types).
     * @see #getInfo(Class) shortcut method
     */
    public <T extends ItemInfo> List<T> getInfos(final Class<?> type) {
        return getData().getInfos(type);
    }

    // --------------------------------------------------------------------------- COMMANDS

    /**
     * Note: only commands installed after classpath scan are tracked and so there might be only
     * one instance for each command type (in spite of the fact that commands descriptors are instance based).
     *
     * @return types of all installed commands or empty list
     * @see ru.vyarus.dropwizard.guice.GuiceBundle.Builder#searchCommands()
     */
    public List<Class<Command>> getCommands() {
        return typesOnly(context.getItems(ConfigItem.Command));
    }

    // --------------------------------------------------------------------------- BUNDLES

    /**
     * Note that multiple instances could be installed for some bundles, but returned list will contain just
     * bundle type (no matter how many instances were actually installed).
     * <p>
     * Important: bundles returned in the registration order; if initialization order is required see
     * {@link #getGuiceyBundlesInInitOrder()} (different because of transitive bundles).
     *
     * @return types of all installed and enabled bundles (including lookup bundles) or empty list
     */
    public List<Class<GuiceyBundle>> getGuiceyBundles() {
        return typesOnly(getGuiceyBundleIds());
    }

    /**
     * Guicey bundle could register another guicey bundle then this transitive bundle would
     * be initialized before the root bundle. This method sorts registered bundles according to
     * actual initialization order (not by when init started, but when init finished -
     * the same order as for transitive dropwizard bundles).
     *
     * @return applied guicey bundles in initialization order.
     */
    @SuppressWarnings("unchecked")
    public List<Class<? extends GuiceyBundle>> getGuiceyBundlesInInitOrder() {
        final List<GuiceyBundleItemInfo> infos = context.getInfos(ConfigItem.Bundle, Filters.enabled());
        return infos.stream()
                // sort by initialization time to move transitive bundles up
                // (by default, bundles in the registration order)
                .sorted(Comparator.comparingInt(GuiceyBundleItemInfo::getInitOrder))
                .map(info -> (Class<? extends GuiceyBundle>) info.getType())
                .collect(Collectors.toList());
    }

    /**
     * Note that this list could be larger then {@link #getGuiceyBundles()} because multiple bundle instances
     * of the same class could be registered.
     *
     * @return types of all enabled normal guice modules or empty list
     * @see ru.vyarus.dropwizard.guice.GuiceBundle.Builder#bundles(GuiceyBundle...)
     * @see ConfigurationInfo#getInfo(ItemId) for loaded model object for id
     */
    public List<ItemId<GuiceyBundle>> getGuiceyBundleIds() {
        return context.getItems(ConfigItem.Bundle, Filters.enabled());
    }

    /**
     * Note that multiple instances could be installed for some bundles, but returned list will contain just
     * bundle type (no matter how many instances were actually installed).
     *
     * @return types of all installed and enabled dropwizard bundles or empty list
     */
    public List<Class<ConfiguredBundle>> getDropwizardBundles() {
        return typesOnly(getDropwizardBundleIds());
    }

    /**
     * Note that this list could be larger then {@link #getDropwizardBundles()} because multiple bundle instances
     * of the same class could be registered.
     *
     * @return types of all enabled normal guice modules or empty list
     * @see ru.vyarus.dropwizard.guice.GuiceBundle.Builder#bundles(GuiceyBundle...)
     * @see ConfigurationInfo#getInfo(ItemId) for loaded model object for id
     */
    public List<ItemId<ConfiguredBundle>> getDropwizardBundleIds() {
        return context.getItems(ConfigItem.DropwizardBundle, Filters.enabled());
    }

    /**
     * Bundles lookup mechanism could register only one instance of bundle type, but other instances may be
     * registered directly (anyway, it is correct that only one bundle instance belongs to lookup scope).
     *
     * @return types of bundles resolved by bundle lookup mechanism or empty list
     * @see ru.vyarus.dropwizard.guice.bundle.GuiceyBundleLookup
     */
    public List<Class<GuiceyBundle>> getBundlesFromLookup() {
        return typesOnly(context.getItems(ConfigItem.Bundle, Filters.lookupBundles()));
    }

    /**
     * @return all (guicey and dropwizard) enabled top-level bundles (without transitives)
     */
    public List<Class<Object>> getDirectBundles() {
        return typesOnly(context.getItems(Filters.bundles()
                .and(Filters.enabled())
                .and(Filters.transitiveBundles().negate())));
    }

    /**
     * Note that the same type may appear in both enabled and disabled lists if it is instance item and
     * only some instances were disabled.
     *
     * @return types of manually disabled bundles or empty list
     */
    public List<Class<Object>> getBundlesDisabled() {
        return typesOnly(context.getItems(Filters.bundles()
                .and(Filters.enabled().negate())));
    }

    /**
     * @param bundle bundle
     * @return types of bundles (guicey and dropwizard) registered from provided bundle avoiding disabled bundles
     * or empty list
     */
    public List<Class<Object>> getRelativelyInstalledBundles(final Class<?> bundle) {
        return typesOnly(context.getItems(Filters.bundles()
                .and(Filters.enabled())
                .and(Filters.registrationScope(bundle))));
    }

    // --------------------------------------------------------------------------- MODULES

    /**
     * Note that multiple instances could be installed for some modules, but returned list will contain just
     * module type (no matter how many instances were actually installed).
     *
     * @return types of all registered and enabled guice modules (including normal and overriding) or empty list
     */
    public List<Class<Module>> getModules() {
        return typesOnly(getModuleIds());
    }

    /**
     * Note that this list could be larger then {@link #getModules()} because multiple module instances of the same
     * class could be registered.
     *
     * @return ids of all registered and enabled guice modules (including normal and overriding) or empty list
     * @see ConfigurationInfo#getInfo(ItemId) for loaded model object for id
     */
    public List<ItemId<Module>> getModuleIds() {
        return context.getItems(ConfigItem.Module, Filters.enabled());
    }

    /**
     * @return types of all enabled normal guice modules or empty list
     * @see ru.vyarus.dropwizard.guice.GuiceBundle.Builder#modules(Module...)
     */
    public List<Class<Module>> getNormalModules() {
        return typesOnly(getNormalModuleIds());
    }

    /**
     * Note that this list could be larger then {@link #getNormalModules()} because multiple module instances
     * of the same class could be registered.
     *
     * @return types of all enabled normal guice modules or empty list
     * @see ru.vyarus.dropwizard.guice.GuiceBundle.Builder#modules(Module...)
     * @see ConfigurationInfo#getInfo(ItemId) for loaded model object for id
     */
    public List<ItemId<Module>> getNormalModuleIds() {
        return context.getItems(ConfigItem.Module, Filters.<ModuleItemInfo>enabled()
                .and(Filters.overridingModule().negate()));
    }

    /**
     * @return types of all enabled overriding guice modules or empty list
     * @see ru.vyarus.dropwizard.guice.GuiceBundle.Builder#modulesOverride(Module...)
     */
    public List<Class<Module>> getOverridingModules() {
        return typesOnly(getOverridingModuleIds());
    }

    /**
     * Note that this list could be larger then {@link #getOverridingModules()} because multiple module instances
     * of the same class could be registered.
     *
     * @return types of all enabled normal guice modules or empty list
     * @see ru.vyarus.dropwizard.guice.GuiceBundle.Builder#modulesOverride(Module...)
     * @see ConfigurationInfo#getInfo(ItemId) for loaded model object for id
     */
    public List<ItemId<Module>> getOverridingModuleIds() {
        return context.getItems(ConfigItem.Module, Filters.<ModuleItemInfo>enabled()
                .and(Filters.overridingModule()));
    }

    /**
     * @return types of manually disabled modules or empty list
     */
    public List<Class<Module>> getModulesDisabled() {
        return typesOnly(context.getItems(ConfigItem.Module, Filters.enabled().negate()));
    }

    // --------------------------------------------------------------------------- INSTALLERS

    /**
     * @return types of all registered installers (without disabled) or empty list
     */
    public List<Class<FeatureInstaller>> getInstallers() {
        return typesOnly(context.getItems(ConfigItem.Installer, Filters.enabled()));
    }

    /**
     * @return installer types, resolved by classpath scan (without disabled) or empty list
     */
    public List<Class<FeatureInstaller>> getInstallersFromScan() {
        return typesOnly(context.getItems(ConfigItem.Installer, Filters.enabled().and(Filters.fromScan())));
    }

    /**
     * @return types of manually disabled installers or empty list
     */
    public List<Class<FeatureInstaller>> getInstallersDisabled() {
        return typesOnly(context.getItems(ConfigItem.Installer, Filters.enabled().negate()));
    }

    /**
     * Returned installers are ordered not by execution order
     * (according to {@link ru.vyarus.dropwizard.guice.module.installer.order.Order} annotations).
     *
     * @return ordered list of installers or empty list
     */
    @SuppressWarnings("unchecked")
    public List<Class<FeatureInstaller>> getInstallersOrdered() {
        return (List) holder.getInstallerTypes();
    }

    // --------------------------------------------------------------------------- EXTENSIONS

    /**
     * @return all registered and enabled extension types (including resolved with classpath scan) or empty list
     */
    public List<Class<Object>> getExtensions() {
        return typesOnly(context.getItems(ConfigItem.Extension, Filters.enabled()));
    }

    /**
     * @param installer installer type
     * @return list of extensions installed by provided installer or empty list
     */
    public List<Class<Object>> getExtensions(final Class<? extends FeatureInstaller> installer) {
        return typesOnly(context.getItems(ConfigItem.Extension, Filters.installedBy(installer)));
    }

    /**
     * @return enabled extension types, resolved by classpath scan or empty list
     */
    public List<Class<Object>> getExtensionsFromScan() {
        return typesOnly(context.getItems(ConfigItem.Extension,
                Filters.<ExtensionItemInfo>enabled().and(Filters.fromScan())));
    }

    /**
     * @return enabled extension types, resolved by guice bindings scan or empty list
     */
    public List<Class<Object>> getExtensionsFromBindings() {
        return typesOnly(context.getItems(ConfigItem.Extension,
                Filters.<ExtensionItemInfo>enabled().and(Filters.fromBinding())));
    }

    /**
     * Returned extensions may be also found by classpath scan or in guice bindings.
     *
     * @return enabled extensions which was registered manually or empty list
     */
    public List<Class<Object>> getExtensionsRegisteredManually() {
        return typesOnly(context.getItems(ConfigItem.Extension,
                Filters.<ExtensionItemInfo>enabled()
                        .and(it -> it.getRegistrationScopeType().equals(ConfigScope.Application)
                                || it.getRegistrationScopeType().equals(ConfigScope.GuiceyBundle))));
    }

    /**
     * One extension could be installed manually then found by classpath scan and then found from guice binding.
     * This method returns only extensions configured manually and never detected by other methods.
     *
     * @return list of enabled extensions, registered only manually or empty list
     */
    public List<Class<Object>> getExtensionsRegisteredManauallyOnly() {
        return typesOnly(context.getItems(ConfigItem.Extension,
                Filters.<ExtensionItemInfo>enabled()
                        .and(Filters.fromScan().negate())
                        .and(Filters.fromBinding().negate())));
    }

    /**
     * Returned installers are ordered by execution order according to
     * {@link ru.vyarus.dropwizard.guice.module.installer.order.Order} annotation.
     * Note: not all installers supports ordering (in any case returned order is the same order as
     * extensions were processed by installer).
     *
     * @param installer installer type
     * @return ordered list of extensions installed by provided installer or empty list
     */
    @SuppressWarnings("unchecked")
    public List<Class<Object>> getExtensionsOrdered(final Class<? extends FeatureInstaller> installer) {
        final List<Class<?>> extensions = holder.getExtensions(installer);
        return extensions == null ? Collections.emptyList() : (List) extensions;
    }

    /**
     * @return types of manually disabled extensions or empty list
     */
    public List<Class<Object>> getExtensionsDisabled() {
        return typesOnly(context.getItems(ConfigItem.Extension, Filters.enabled().negate()));
    }

}
