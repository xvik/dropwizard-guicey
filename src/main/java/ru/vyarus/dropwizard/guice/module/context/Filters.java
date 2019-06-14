package ru.vyarus.dropwizard.guice.module.context;

import ru.vyarus.dropwizard.guice.module.context.info.BundleItemInfo;
import ru.vyarus.dropwizard.guice.module.context.info.ExtensionItemInfo;
import ru.vyarus.dropwizard.guice.module.context.info.ItemInfo;
import ru.vyarus.dropwizard.guice.module.context.info.ModuleItemInfo;
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
    public static <T extends ItemInfo> Predicate<T> disabledBy(final Class<?> scope) {
        return input -> input instanceof DisableSupport && ((DisableSupport) input).getDisabledBy().contains(scope);
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
     * Shortcut for {@link #registrationScope(Class)} for special scopes (like classpath scan, bundles lookup etc).
     *
     * @param specialScope special scope type
     * @param <T>          expected info container type (if used within single configuration type)
     * @return items registered in specified context filter
     */
    public static <T extends ItemInfo> Predicate<T> registrationScope(final ConfigScope specialScope) {
        return registrationScope(specialScope.getType());
    }

    /**
     * Filter for items registered by specified context. Context class could be
     * {@link io.dropwizard.Application}, {@link ru.vyarus.dropwizard.guice.module.installer.scanner.ClasspathScanner},
     * {@link io.dropwizard.Bundle}, {@link ru.vyarus.dropwizard.guice.bundle.GuiceyBundleLookup} and
     * classes implementing {@link ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBundle}.
     * Safe to apply filter for all items.
     * <p>
     * Note: counts only actual registration, ignoring duplicate (rejected) registrations
     * (see {@link #registeredBy(Class)} for filter counting all registrations).
     *
     * @param scope scope class
     * @param <T>   expected info container type (if used within single configuration type)
     * @return items registered in specified context filter
     * @see ConfigScope for the list of all special scopes
     */
    public static <T extends ItemInfo> Predicate<T> registrationScope(final Class<?> scope) {
        return input -> scope.equals(input.getRegistrationScope());
    }

    /**
     * Shortcut for {@link #registeredBy(Class)} for special scopes (like classpath scan, bundles lookup etc).
     *
     * @param specialScope special scope type
     * @param <T>          expected info container type (if used within single configuration type)
     * @return items registered in specified context filter
     */
    public static <T extends ItemInfo> Predicate<T> registeredBy(final ConfigScope specialScope) {
        return registeredBy(specialScope.getType());
    }

    /**
     * In contrast to {@link #registrationScope(Class)} this filter returns item for all scopes registered it
     * (not only for first registered scope).
     *
     * @param type context class
     * @param <T>  expected info container type (if used within single configuration type)
     * @return items registered in specified context filter
     * @see ConfigScope for the list of all special scopes
     */
    public static <T extends ItemInfo> Predicate<T> registeredBy(final Class<?> type) {
        return input -> input.getRegisteredBy().contains(type);
    }

    /**
     * Filter used for multi-type searches to filter out item types.
     * In order to filter out only one type, may be used in conjunction with
     * {@link Predicate#negate()}.
     *
     * @param types item types to match
     * @return items of type filter
     */
    public static Predicate<ItemInfo> type(final ConfigItem... types) {
        final List<ConfigItem> target = Arrays.asList(types);
        return input -> target.contains(input.getItemType());
    }

    // --------------------------------------------------------------------------- BUNDLES

    /**
     * Filter for bundles resolved by lookup mechanism. Use only for {@link ConfigItem#Bundle} items.
     *
     * @return bundles resolved by lookup filter
     */
    public static Predicate<BundleItemInfo> lookupBundles() {
        return BundleItemInfo::isFromLookup;
    }

    /**
     * Filter for transitive bundles: bundles registered only by other bundles (and never directly).
     *
     * @return transitive bundled filter
     */
    public static Predicate<BundleItemInfo> transitiveBundles() {
        return BundleItemInfo::isTransitive;
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
