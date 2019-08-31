package ru.vyarus.dropwizard.guice.module.context;

import ru.vyarus.dropwizard.guice.module.context.info.*;
import ru.vyarus.dropwizard.guice.module.context.info.sign.DisableSupport;
import ru.vyarus.dropwizard.guice.module.context.info.sign.ScanSupport;
import ru.vyarus.dropwizard.guice.module.installer.FeatureInstaller;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

/**
 * Common filters for configuration information filtering in
 * {@link ConfigurationInfo#getItems(ConfigItem, Predicate)} and
 * {@link ConfigurationInfo#getItems(Predicate)}.
 * Use {@link Predicate#and(Predicate)}, {@link Predicate#or(Predicate)} and {@link Predicate#negate()}
 * to reuse default filters.
 *
 * @author Vyacheslav Rusakov
 * @since 06.07.2016
 */
public final class Filters {

    private Filters() {
    }

    // --------------------------------------------------------------------------- GENERIC

    /**
     * Filter for enabled items. Not all items support disable ({@link DisableSupport}).
     * Items not supporting disable considered enabled (so it's safe to apply filter for
     * all items).
     *
     * @param <T> expected info container type (if used within single configuration type)
     * @return enabled items filter
     */
    public static <T extends ItemInfo> Predicate<T> enabled() {
        return input -> !(input instanceof DisableSupport) || ((DisableSupport) input).isEnabled();
    }

    /**
     * Filter for items disabled in specified scope. Not all items support disable ({@link DisableSupport}).
     * Items not supporting disable considered enabled (so it's safe to apply filter for
     * all items).
     *
     * @param scope target scope
     * @param <T>   expected info container type (if used within single configuration type)
     * @return items disabled in scope filter
     */
    public static <T extends ItemInfo> Predicate<T> disabledBy(final ItemId scope) {
        return input -> input instanceof DisableSupport && ((DisableSupport) input).getDisabledBy().contains(scope);
    }

    /**
     * Shortcut for {@link #disabledBy(ItemId)}.
     *
     * @param type target scope type
     * @param <T>  expected info container type (if used within single configuration type)
     * @return items disabled in scope filter
     */
    public static <T extends ItemInfo> Predicate<T> disabledBy(final Class<?> type) {
        return disabledBy(ItemId.from(type));
    }

    /**
     * Filter for items registered with classpath scan. Not all items support classpath scan
     * {@link ScanSupport}. Items not supporting scan are considered not resolved by scan
     * (so it's safe to apply filter for all items).
     *
     * @param <T> expected info container type (if used within single configuration type)
     * @return items from classpath scan filter
     */
    public static <T extends ItemInfo> Predicate<T> fromScan() {
        return input -> input instanceof ScanSupport && ((ScanSupport) input).isFromScan();
    }

    /**
     * Filter for items registered by specified context. Context class could be
     * {@link io.dropwizard.Application}, {@link ru.vyarus.dropwizard.guice.module.installer.scanner.ClasspathScanner},
     * {@link ru.vyarus.dropwizard.guice.bundle.GuiceyBundleLookup} and
     * classes implementing {@link ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBundle}.
     * Safe to apply filter for all items.
     * <p>
     * Note: counts only actual registration, ignoring duplicate (rejected) registrations
     * (see {@link #registeredBy(Class)} for filter counting all registrations).
     * <p>
     * Note: if scope key contain class only (without instance identity) and multiple scope instances were registered,
     * then all object from all instance scopes will be returned.
     *
     * @param scope scope class
     * @param <T>   expected info container type (if used within single configuration type)
     * @return items registered in specified context filter
     * @see ConfigScope for the list of all special scopes
     */
    public static <T extends ItemInfo> Predicate<T> registrationScope(final ItemId scope) {
        return input -> scope.equals(input.getRegistrationScope());
    }

    /**
     * Shortcut for {@link #registrationScope(ItemId)} for special scopes (like classpath scan, bundles lookup etc).
     *
     * @param specialScope special scope type
     * @param <T>          expected info container type (if used within single configuration type)
     * @return items registered in specified context filter
     */
    public static <T extends ItemInfo> Predicate<T> registrationScope(final ConfigScope specialScope) {
        return registrationScope(specialScope.getType());
    }

    /**
     * Shortcut for {@link #registrationScope(ItemId)}.
     * <p>
     * Note: if type is a bundle type and multiple bundle instances were registered, then all registered items from
     * all bundle instances will be returned (will affect multiple scopes).
     *
     * @param type scope type
     * @param <T>  expected info container type (if used within single configuration type)
     * @return items registered in specified context filter
     */
    public static <T extends ItemInfo> Predicate<T> registrationScope(final Class<?> type) {
        return registrationScope(ItemId.from(type));
    }

    /**
     * In contrast to {@link #registrationScope(ItemId)} this filter returns item for all scopes mentioned item
     * (including scopes where registration was considered duplicate).
     *
     * @param scope scope key
     * @param <T>   expected info container type (if used within single configuration type)
     * @return items registered in specified context filter
     * @see ConfigScope for the list of all special scopes
     */
    public static <T extends ItemInfo> Predicate<T> registeredBy(final ItemId scope) {
        return input -> input.getRegisteredBy().contains(scope);
    }

    /**
     * Shortcut for {@link #registeredBy(ItemId)} for special scopes (like classpath scan, bundles lookup etc).
     *
     * @param specialScope special scope type
     * @param <T>          expected info container type (if used within single configuration type)
     * @return items registered in specified context filter
     */
    public static <T extends ItemInfo> Predicate<T> registeredBy(final ConfigScope specialScope) {
        return registeredBy(specialScope.getKey());
    }

    /**
     * Shortcut for {@link #registeredBy(ItemId)} for scope classes.
     *
     * @param type scope class
     * @param <T>  expected info container type (if used within single configuration type)
     * @return items registered in specified context filter
     */
    public static <T extends ItemInfo> Predicate<T> registeredBy(final Class<?> type) {
        return registeredBy(ItemId.from(type));
    }

    /**
     * Filter used for multi-type searches to filter out item types.
     * In order to filter out only one type, may be used in conjunction with
     * {@link Predicate#negate()}.
     *
     * @param types item types to match
     * @param <T>   expected info container type (if used within single configuration type)
     * @return items of type filter
     */
    public static <T extends ItemInfo> Predicate<T> type(final ConfigItem... types) {
        final List<ConfigItem> target = Arrays.asList(types);
        return input -> target.contains(input.getItemType());
    }

    /**
     * Filter used for instance items (bundle, module) selection by type.
     *
     * @param type item class
     * @param <T>  expected info container type (if used within single configuration type)
     * @return items of class filter
     */
    public static <T extends ItemInfo> Predicate<T> type(final Class<?> type) {
        return input -> input.getType().equals(type);
    }

    // --------------------------------------------------------------------------- BUNDLES

    /**
     * Filter for bundles resolved by lookup mechanism. Use only for {@link ConfigItem#Bundle} items.
     *
     * @return bundles resolved by lookup filter
     */
    public static Predicate<GuiceyBundleItemInfo> lookupBundles() {
        return GuiceyBundleItemInfo::isFromLookup;
    }

    /**
     * Filter for transitive bundles: bundles registered only by other bundles (and never directly).
     * Applied to both guicey and dropwizard bundles.
     *
     * @return transitive bundled filter
     */
    public static Predicate<BundleItemInfo> transitiveBundles() {
        return BundleItemInfo::isTransitive;
    }

    /**
     * Guicey and dropwizard bundles are pretty much unified and likely to be queried together for reporting.
     *
     * @param <T> expected info container type (if used within single configuration type)
     * @return bundle predicate (guicey or dropwizard)
     */
    public static <T extends BundleItemInfo> Predicate<T> bundles() {
        return Filters.<T>type(ConfigItem.Bundle).or(type(ConfigItem.DropwizardBundle));
    }

    // --------------------------------------------------------------------------- EXTENSIONS

    /**
     * Filter for extensions installed by specified installer. Use only for {@link ConfigItem#Extension} items.
     *
     * @param type installer class
     * @return extensions installed by specified installer filter
     */
    public static Predicate<ExtensionItemInfo> installedBy(final Class<? extends FeatureInstaller> type) {
        return input -> type.equals(input.getInstalledBy());
    }

    /**
     * Filter for extensions detected from guice bindings. Use only for {@link ConfigItem#Extension} items.
     *
     * @return extensions resolved from binding filter
     */
    public static Predicate<ExtensionItemInfo> fromBinding() {
        return ExtensionItemInfo::isGuiceBinding;
    }

    // --------------------------------------------------------------------------- MODULES

    /**
     * Filter for overriding modules. Use only for {@link ConfigItem#Module} items.
     *
     * @return overriding modules filter
     */
    public static Predicate<ModuleItemInfo> overridingModule() {
        return ModuleItemInfo::isOverriding;
    }
}
