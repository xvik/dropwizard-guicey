package ru.vyarus.dropwizard.guice.module.context;

import com.google.common.base.Preconditions;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.inject.Module;
import io.dropwizard.Application;
import io.dropwizard.Bundle;
import io.dropwizard.cli.Command;
import ru.vyarus.dropwizard.guice.bundle.GuiceyBundleLookup;
import ru.vyarus.dropwizard.guice.module.context.info.ItemInfo;
import ru.vyarus.dropwizard.guice.module.context.info.impl.ExtensionItemInfoImpl;
import ru.vyarus.dropwizard.guice.module.context.info.impl.ItemInfoImpl;
import ru.vyarus.dropwizard.guice.module.context.info.sign.DisableSupport;
import ru.vyarus.dropwizard.guice.module.context.option.Option;
import ru.vyarus.dropwizard.guice.module.context.option.internal.OptionsSupport;
import ru.vyarus.dropwizard.guice.module.context.stat.StatsTracker;
import ru.vyarus.dropwizard.guice.module.installer.FeatureInstaller;
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBundle;
import ru.vyarus.dropwizard.guice.module.installer.scanner.ClasspathScanner;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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
@SuppressWarnings("PMD.GodClass")
public final class ConfigurationContext {

    /**
     * Configured items (bundles, installers, extensions etc).
     * Preserve registration order.
     */
    private final Multimap<ConfigItem, Object> itemsHolder = LinkedHashMultimap.create();
    /**
     * Configuration details (stored mostly for diagnostics).
     */
    private final Map<Class<?>, ItemInfo> detailsHolder = Maps.newHashMap();
    /**
     * Holds disabled entries separately. Preserve registration order.
     */
    private final Multimap<ConfigItem, Class<?>> disabledItemsHolder = LinkedHashMultimap.create();
    /**
     * Holds disable source for disabled items.
     */
    private final Multimap<Class<?>, Class<?>> disabledByHolder = LinkedHashMultimap.create();
    /**
     * Current scope hierarchy. The last one is actual scope (application or bundle).
     */
    private Class<?> currentScope;

    /**
     * Used to gather guicey startup metrics.
     */
    private final StatsTracker tracker = new StatsTracker();
    /**
     * Used to set and get options within guicey.
     */
    private final OptionsSupport optionsSupport = new OptionsSupport();


    // --------------------------------------------------------------------------- SCOPE

    /**
     * Current configuration context (application, bundle or classpath scan).
     *
     * @param scope scope class
     */
    public void setScope(final Class<?> scope) {
        Preconditions.checkState(currentScope == null, "State error: current scope not closed");
        currentScope = scope;
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
        setScope(ClasspathScanner.class);
        for (Class<Command> cmd : commands) {
            register(ConfigItem.Command, cmd);
        }
        closeScope();
    }

    // --------------------------------------------------------------------------- BUNDLES

    /**
     * Register bundles, recognized from dropwizard bundles. {@link Bundle} used as context.
     *
     * @param bundles recognized bundles
     * @see ru.vyarus.dropwizard.guice.GuiceBundle.Builder#configureFromDropwizardBundles()
     */
    public void registerDwBundles(final List<GuiceyBundle> bundles) {
        setScope(Bundle.class);
        for (GuiceyBundle bundle : bundles) {
            register(ConfigItem.Bundle, bundle);
        }
        closeScope();
    }

    /**
     * Register bundles resolved by lookup mechanism. {@link GuiceyBundleLookup} used as context.
     *
     * @param bundles bundles resolved by lookup mechanism
     * @see GuiceyBundleLookup
     */
    public void registerLookupBundles(final List<GuiceyBundle> bundles) {
        setScope(GuiceyBundleLookup.class);
        for (GuiceyBundle bundle : bundles) {
            register(ConfigItem.Bundle, bundle);
        }
        closeScope();
    }

    /**
     * Usual bundle registration from {@link ru.vyarus.dropwizard.guice.GuiceBundle.Builder#bundles(GuiceyBundle...)}
     * or {@link ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBootstrap#bundles(GuiceyBundle...)}.
     * Context class is set to currently processed bundle.
     *
     * @param bundles bundles to register
     */
    public void registerBundles(final GuiceyBundle... bundles) {
        for (GuiceyBundle bundle : bundles) {
            register(ConfigItem.Bundle, bundle);
        }
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
            registerDisable(ConfigItem.Bundle, bundle);
        }
    }

    /**
     * @return all configured bundles (without duplicates)
     */
    public List<GuiceyBundle> getEnabledBundles() {
        return getEnabledItems(ConfigItem.Bundle);
    }

    /**
     * Bundle must be disabled before it's processing, otherwise disabling will not have effect
     * (because bundle will be already processed and register all related items).
     *
     * @return true if bundle enabled, false otherwise
     */
    public boolean isBundleEnabled(final Class<? extends GuiceyBundle> type) {
        return isEnabled(ConfigItem.Bundle, type);
    }

    // --------------------------------------------------------------------------- MODULES

    /**
     * @param modules guice module to register
     */
    public void registerModules(final Module... modules) {
        for (Module module : modules) {
            register(ConfigItem.Module, module);
        }
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
            registerDisable(ConfigItem.Module, module);
        }
    }

    /**
     * @return all configured and enabled guice modules (without duplicates)
     */
    public List<Module> getEnabledModules() {
        return getEnabledItems(ConfigItem.Module);
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
        setScope(ClasspathScanner.class);
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
            registerDisable(ConfigItem.Installer, installer);
        }
    }

    /**
     * @return all configured and enabled installers (including resolved by scan)
     */
    public List<Class<? extends FeatureInstaller>> getEnabledInstallers() {
        return getEnabledItems(ConfigItem.Installer);
    }

    // --------------------------------------------------------------------------- EXTENSIONS

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
            setScope(ClasspathScanner.class);
            info = register(ConfigItem.Extension, extension);
            closeScope();
        } else {
            // info will be available for sure because such type was stored before (during manual registration)
            info = getInfo(extension);
        }

        return info;
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
            registerDisable(ConfigItem.Extension, extension);
        }
    }

    /**
     * @param extension extension type
     * @return true if extension is enabled, false if disabled
     */
    public boolean isExtensionEnabled(final Class<?> extension) {
        return isEnabled(ConfigItem.Extension, extension);
    }

    /**
     * @return all configured extensions (including resolved by scan)
     */
    public List<Class<?>> getEnabledExtensions() {
        return getEnabledItems(ConfigItem.Extension);
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

    // --------------------------------------------------------------------------- GENERAL


    /**
     * Called when context configuration is finished (but extensions installation is not finished yet).
     * Merges disabled items configuration with registered items or creates new items to hold disable info.
     */
    public void finalizeConfiguration() {
        for (ConfigItem type : disabledItemsHolder.keys()) {
            for (Object item : getDisabledItems(type)) {
                final DisableSupport info = getOrCreateInfo(type, item);
                info.getDisabledBy().addAll(disabledByHolder.get(getType(item)));
            }
        }
    }

    /**
     * @param type config type
     * @param <T>  expected container type
     * @return list of all registered items of type or empty list
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> getItems(final ConfigItem type) {
        final Collection<Object> res = itemsHolder.get(type);
        return res.isEmpty() ? Collections.<T>emptyList() : (List<T>) Lists.newArrayList(res);
    }

    /**
     * Note: registration is always tracked by class, so when instance provided actual info will be returned
     * for object class.
     *
     * @param item item to get info
     * @param <T>  expected container type
     * @return item registration info container or null if item not registered
     */
    @SuppressWarnings("unchecked")
    public <T extends ItemInfoImpl> T getInfo(final Object item) {
        final Class<?> itemType = getType(item);
        return (T) detailsHolder.get(itemType);
    }

    /**
     * @return startup statistics tracker instance
     */
    public StatsTracker stat() {
        return tracker;
    }

    private Class<?> getScope() {
        return currentScope == null ? Application.class : currentScope;
    }

    private void registerDisable(final ConfigItem type, final Class<?> item) {
        // multimaps will filter duplicates automatically
        disabledItemsHolder.put(type, item);
        disabledByHolder.put(item, getScope());
    }

    private <T extends ItemInfoImpl> T register(final ConfigItem type, final Object item) {
        final T info = getOrCreateInfo(type, item);
        // if registered multiple times in one scope attempts will reveal it
        info.countRegistrationAttempt();
        info.getRegisteredBy().add(getScope());
        // first registration scope stored
        if (info.getRegistrationScope() == null) {
            info.setRegistrationScope(getScope());
        }
        return info;
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
        final Class<?> itemType = getType(item);
        final T info;
        // details holder allows to implicitly filter by type and avoid duplicate registration
        if (detailsHolder.containsKey(itemType)) {
            // no duplicate registration
            info = (T) detailsHolder.get(itemType);
        } else {
            itemsHolder.put(type, item);
            info = type.newContainer(itemType);
            detailsHolder.put(itemType, info);
        }
        return info;
    }

    private Class<?> getType(final Object item) {
        return item instanceof Class ? (Class) item : item.getClass();
    }

    @SuppressWarnings("unchecked")
    private <T> List<T> getDisabledItems(final ConfigItem type) {
        final Collection<Class<?>> res = disabledItemsHolder.get(type);
        return res.isEmpty() ? Collections.<T>emptyList() : (List<T>) Lists.newArrayList(res);
    }

    private <T> List<T> getEnabledItems(final ConfigItem type) {
        final List<T> res = getItems(type);
        res.removeAll(getDisabledItems(type));
        return res;
    }

    private boolean isEnabled(final ConfigItem type, final Class itemType) {
        return !disabledItemsHolder.get(type).contains(itemType);
    }
}
