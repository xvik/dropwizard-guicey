package ru.vyarus.dropwizard.guice.module;

import com.google.common.collect.Sets;
import com.google.inject.Module;
import io.dropwizard.cli.Command;
import ru.vyarus.dropwizard.guice.module.context.ConfigItem;
import ru.vyarus.dropwizard.guice.module.context.ConfigScope;
import ru.vyarus.dropwizard.guice.module.context.ConfigurationInfo;
import ru.vyarus.dropwizard.guice.module.context.Filters;
import ru.vyarus.dropwizard.guice.module.context.info.BundleItemInfo;
import ru.vyarus.dropwizard.guice.module.context.info.ExtensionItemInfo;
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
import java.util.List;
import java.util.Set;

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
    public List<Class<Object>> getItemsByScope(final ConfigScope specialScope) {
        return getItemsByScope(specialScope.getType());
    }

    /**
     * NOTE: single item may be registered from multiple scopes! This method will return entity by all it's registered
     * scopes and not just but by first registration. It makes it usable, for example, for configuration tree building.
     * If you need exact registration scope use {@link Filters#registrationScope(Class)} filter.
     *
     * @param scope required scope
     * @return all enabled items registered in specified scope or empty list
     * @see ItemInfo#getRegisteredBy() for more info about scopes
     * @see ConfigScope for the list of all special scopes
     */
    public List<Class<Object>> getItemsByScope(final Class<?> scope) {
        return context.getItems(Filters.enabled().and(Filters.registeredBy(scope)));
    }

    /**
     * @return all ative scopes including disable only scopes
     * @see #getActiveScopes(boolean)
     * @see ConfigScope for the list of all special scopes
     */
    public Set<Class<?>> getActiveScopes() {
        return getActiveScopes(true);
    }

    /**
     * @param countDisables include scopes with disables only
     * @return all active scopes or empty collection
     * @see ItemInfo#getRegisteredBy() for more info about scopes
     */
    public Set<Class<?>> getActiveScopes(final boolean countDisables) {
        final Set<Class<?>> res = Sets.newHashSet();
        context.getItems(it -> {
            res.addAll(it.getRegisteredBy());
            if (countDisables && it instanceof DisableSupport) {
                res.addAll(((DisableSupport) it).getDisabledBy());
            }
            return true;
        });
        return res;
    }

    // --------------------------------------------------------------------------- COMMANDS

    /**
     * @return types of all installed commands or empty list
     * @see ru.vyarus.dropwizard.guice.GuiceBundle.Builder#searchCommands()
     */
    public List<Class<Command>> getCommands() {
        return context.getItems(ConfigItem.Command);
    }

    // --------------------------------------------------------------------------- BUNDLES

    /**
     * @return types of all installed and enabled bundles (including lookup bundles) or empty list
     */
    public List<Class<GuiceyBundle>> getBundles() {
        return context.getItems(ConfigItem.Bundle, Filters.enabled());
    }

    /**
     * @return types of bundles resolved by bundle lookup mechanism or empty list
     * @see ru.vyarus.dropwizard.guice.bundle.GuiceyBundleLookup
     */
    public List<Class<GuiceyBundle>> getBundlesFromLookup() {
        return context.getItems(ConfigItem.Bundle, Filters.lookupBundles());
    }

    /**
     * @return all enabled top-level bundles (without transitives)
     */
    public List<Class<GuiceyBundle>> getDirectBundles() {
        return context.getItems(ConfigItem.Bundle,
                Filters.<BundleItemInfo>enabled().and(Filters.transitiveBundles().negate()));
    }

    /**
     * @return types of manually disabled bundles or empty list
     */
    public List<Class<GuiceyBundle>> getBundlesDisabled() {
        return context.getItems(ConfigItem.Bundle, Filters.enabled().negate());
    }

    // --------------------------------------------------------------------------- MODULES

    /**
     * @return types of all registered and enabled guice modules (including normal and overriding) or empty list
     */
    public List<Class<Module>> getModules() {
        return context.getItems(ConfigItem.Module, Filters.enabled());
    }

    /**
     * @return types of all enabled normal guice modules or empty list
     * @see ru.vyarus.dropwizard.guice.GuiceBundle.Builder#modules(Module...)
     */
    public List<Class<Module>> getNormalModules() {
        return context.getItems(ConfigItem.Module, Filters.<ModuleItemInfo>enabled()
                .and(Filters.overridingModule().negate()));
    }

    /**
     * @return types of all enabled overriding guice modules or empty list
     * @see ru.vyarus.dropwizard.guice.GuiceBundle.Builder#modulesOverride(Module...)
     */
    public List<Class<Module>> getOverridingModules() {
        return context.getItems(ConfigItem.Module, Filters.<ModuleItemInfo>enabled()
                .and(Filters.overridingModule()));
    }

    /**
     * @return types of manually disabled modules or empty list
     */
    public List<Class<Module>> getModulesDisabled() {
        return context.getItems(ConfigItem.Module, Filters.enabled().negate());
    }

    // --------------------------------------------------------------------------- INSTALLERS

    /**
     * @return types of all registered installers (without disabled) or empty list
     */
    public List<Class<FeatureInstaller>> getInstallers() {
        return context.getItems(ConfigItem.Installer, Filters.enabled());
    }

    /**
     * @return installer types, resolved by classpath scan (without disabled) or empty list
     */
    public List<Class<FeatureInstaller>> getInstallersFromScan() {
        return context.getItems(ConfigItem.Installer, Filters.enabled().and(Filters.fromScan()));
    }

    /**
     * @return types of manually disabled installers or empty list
     */
    public List<Class<FeatureInstaller>> getInstallersDisabled() {
        return context.getItems(ConfigItem.Installer, Filters.enabled().negate());
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
        return context.getItems(ConfigItem.Extension, Filters.enabled());
    }

    /**
     * @param installer installer type
     * @return list of extensions installed by provided installer or empty list
     */
    public List<Class<Object>> getExtensions(final Class<? extends FeatureInstaller> installer) {
        return context.getItems(ConfigItem.Extension, Filters.installedBy(installer));
    }

    /**
     * @return enabled extension types, resolved by classpath scan or empty list
     */
    public List<Class<Object>> getExtensionsFromScan() {
        return context.getItems(ConfigItem.Extension, Filters.<ExtensionItemInfo>enabled()
                .and(Filters.fromScan()));
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
        return context.getItems(ConfigItem.Extension, Filters.enabled().negate());
    }

}
