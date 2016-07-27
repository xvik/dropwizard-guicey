package ru.vyarus.dropwizard.guice.module;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Sets;
import com.google.inject.Module;
import io.dropwizard.cli.Command;
import ru.vyarus.dropwizard.guice.module.context.ConfigItem;
import ru.vyarus.dropwizard.guice.module.context.ConfigurationInfo;
import ru.vyarus.dropwizard.guice.module.context.Filters;
import ru.vyarus.dropwizard.guice.module.context.info.ItemInfo;
import ru.vyarus.dropwizard.guice.module.context.info.impl.ExtensionItemInfoImpl;
import ru.vyarus.dropwizard.guice.module.context.info.impl.InstallerItemInfoImpl;
import ru.vyarus.dropwizard.guice.module.installer.FeatureInstaller;
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBundle;
import ru.vyarus.dropwizard.guice.module.installer.internal.ExtensionsHolder;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.google.common.base.Predicates.not;

/**
 * Public api for internal guicey configuration info. Provides information about registered bundle types,
 * installers, extensions, disabled installers etc. Registered as guice bean and could be directly injected.
 * <p>
 * Could be used for configuration diagnostics or unit test checks.
 *
 * @author Vyacheslav Rusakov
 * @since 21.06.2016
 */
public class GuiceyConfigurationInfo {

    private final ConfigurationInfo context;
    private final ExtensionsHolder holder;

    @Inject
    public GuiceyConfigurationInfo(final ConfigurationInfo context, final ExtensionsHolder holder) {
        this.context = context;
        this.holder = holder;
    }

    /**
     * Use to perform custom data lookups (e.g. for additional logging, diagnostics or consistency checks).
     *
     * @return raw configuration info object
     */
    public ConfigurationInfo getData() {
        return context;
    }

    /**
     * NOTE: single item may be registered from multiple scopes! This method will return entity by all it's registered
     * scopes and not just but by first registration. It makes it usable, for example, for configuration tree building.
     * If you need exact registration scope use {@link Filters#registrationScope(Class)} filter.
     *
     * @param scope required scope
     * @return all enabled items registered in specified scope or empty list
     * @see ItemInfo#getRegisteredBy() for more info about scopes
     */
    public List<Class<Object>> getItemsByScope(final Class<?> scope) {
        return context.getItems(Predicates.and(Filters.enabled(), Filters.registeredBy(scope)));
    }

    /**
     * @return all active scopes or empty collection
     * @see ItemInfo#getRegisteredBy() for more info about scopes
     */
    public Set<Class<?>> getActiveScopes() {
        final Set<Class<?>> res = Sets.newHashSet();
        context.getItems(new Predicate<ItemInfo>() {
            @Override
            public boolean apply(final @Nonnull ItemInfo input) {
                res.addAll(input.getRegisteredBy());
                return false;
            }
        });
        return res;
    }

    // --------------------------------------------------------------------------- COMMANDS

    /**
     * @return types of all installed commands or empty list
     * @see ru.vyarus.dropwizard.guice.GuiceBundle.Builder#searchCommands(boolean)
     */
    public List<Class<Command>> getCommands() {
        return context.getItems(ConfigItem.Command);
    }

    // --------------------------------------------------------------------------- BUNDLES

    /**
     * @return types of all installed bundles (including lookup bundles) or empty list
     */
    public List<Class<GuiceyBundle>> getBundles() {
        return context.getItems(ConfigItem.Bundle);
    }

    /**
     * @return types of bundles resolved by bundle lookup mechanism or empty list
     * @see ru.vyarus.dropwizard.guice.bundle.GuiceyBundleLookup
     */
    public List<Class<GuiceyBundle>> getBundlesFromLookup() {
        return context.getItems(ConfigItem.Bundle, Filters.lookupBundles());
    }

    /**
     * @return types of bundles resolved from dropwizard bundles or empty list
     * @see ru.vyarus.dropwizard.guice.GuiceBundle.Builder#configureFromDropwizardBundles(boolean)
     */
    public List<Class<GuiceyBundle>> getBundlesFromDw() {
        return context.getItems(ConfigItem.Bundle, Filters.dwBundles());
    }

    // --------------------------------------------------------------------------- MODULES

    /**
     * @return types of all registered guice modules or empty list
     */
    public List<Class<Module>> getModules() {
        return context.getItems(ConfigItem.Module);
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
        return context.getItems(ConfigItem.Installer,
                Predicates.<InstallerItemInfoImpl>and(Filters.enabled(), Filters.fromScan()));
    }

    /**
     * @return types of manually disabled installers or empty list
     */
    public List<Class<FeatureInstaller>> getInstallersDisabled() {
        return context.getItems(ConfigItem.Installer, not(Filters.enabled()));
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
     * @return all registered extension types (including resolved with classpath scan) or empty list
     */
    public List<Class<Object>> getExtensions() {
        return context.getItems(ConfigItem.Extension);
    }

    /**
     * @param installer installer type
     * @return list of extensions installed by provided installer or empty list
     */
    public List<Class<Object>> getExtensions(final Class<? extends FeatureInstaller> installer) {
        return context.getItems(ConfigItem.Extension, Filters.installedBy(installer));
    }

    /**
     * @return extension types, resolved by classpath scan or empty list
     */
    public List<Class<Object>> getExtensionsFromScan() {
        return context.getItems(ConfigItem.Extension, Filters.<ExtensionItemInfoImpl>fromScan());
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
}
