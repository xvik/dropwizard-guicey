package ru.vyarus.dropwizard.guice.module.context;

import com.google.common.base.Predicate;
import ru.vyarus.dropwizard.guice.module.context.info.BundleItemInfo;
import ru.vyarus.dropwizard.guice.module.context.info.ExtensionItemInfo;
import ru.vyarus.dropwizard.guice.module.context.info.ItemInfo;
import ru.vyarus.dropwizard.guice.module.context.info.sign.DisableSupport;
import ru.vyarus.dropwizard.guice.module.context.info.sign.ScanSupport;
import ru.vyarus.dropwizard.guice.module.installer.FeatureInstaller;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;

/**
 * Common filters for configuration information filtering in
 * {@link ConfigurationInfo#getItems(ConfigItem, Predicate)} and
 * {@link ConfigurationInfo#getItems(Predicate)}.
 * Use {@link com.google.common.base.Predicates#and(Iterable)},
 * {@link com.google.common.base.Predicates#or(Iterable)} and other composition methods to reuse default
 * filters.
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
        return new Predicate<T>() {
            @Override
            public boolean apply(final @Nonnull T input) {
                return !(input instanceof DisableSupport) || ((DisableSupport) input).isEnabled();
            }
        };
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
        return new Predicate<T>() {
            @Override
            public boolean apply(final @Nonnull T input) {
                return input instanceof DisableSupport && ((DisableSupport) input).getDisabledBy().contains(scope);
            }
        };
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
        return new Predicate<T>() {
            @Override
            public boolean apply(final @Nonnull T input) {
                return input instanceof ScanSupport && ((ScanSupport) input).isFromScan();
            }
        };
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
     */
    public static <T extends ItemInfo> Predicate<T> registrationScope(final Class<?> scope) {
        return new Predicate<T>() {
            @Override
            public boolean apply(final @Nonnull T input) {
                return scope.equals(input.getRegistrationScope());
            }
        };
    }

    /**
     * In contrast to {@link #registrationScope(Class)} this filter returns item for all scopes registered it
     * (not only for first registered scope).
     *
     * @param type context class
     * @param <T>  expected info container type (if used within single configuration type)
     * @return items registered in specified context filter
     */
    public static <T extends ItemInfo> Predicate<T> registeredBy(final Class<?> type) {
        return new Predicate<T>() {
            @Override
            public boolean apply(final @Nonnull T input) {
                return input.getRegisteredBy().contains(type);
            }
        };
    }

    /**
     * Filter used for multi-type searches to filter out item types.
     * In order to filter out only one type, may be used in conjunction with
     * {@link com.google.common.base.Predicates#not(Predicate)}.
     *
     * @param types item types to match
     * @return items of type filter
     */
    public static Predicate<ItemInfo> type(final ConfigItem... types) {
        final List<ConfigItem> target = Arrays.asList(types);
        return new Predicate<ItemInfo>() {
            @Override
            public boolean apply(final @Nonnull ItemInfo input) {
                return target.contains(input.getItemType());
            }
        };
    }

    // --------------------------------------------------------------------------- BUNDLES

    /**
     * Filter for bundles resolved by lookup mechanism. Use only for {@link ConfigItem#Bundle} items.
     *
     * @return bundles resolved by lookup filter
     */
    public static Predicate<BundleItemInfo> lookupBundles() {
        return new Predicate<BundleItemInfo>() {
            @Override
            public boolean apply(final @Nonnull BundleItemInfo input) {
                return input.isFromLookup();
            }
        };
    }

    /**
     * Filter for bundles resolved from dropwizard bundles. Use only for {@link ConfigItem#Bundle} items.
     *
     * @return bundles resolved from dropwizard bundles filter
     */
    public static Predicate<BundleItemInfo> dwBundles() {
        return new Predicate<BundleItemInfo>() {
            @Override
            public boolean apply(final @Nonnull BundleItemInfo input) {
                return input.isFromDwBundle();
            }
        };
    }

    // --------------------------------------------------------------------------- EXTENSIONS

    /**
     * Filter for extensions installed by specified installer. Use only for {@link ConfigItem#Extension} items.
     *
     * @param type installer class
     * @return extensions installed by specified installer filter
     */
    public static Predicate<ExtensionItemInfo> installedBy(final Class<? extends FeatureInstaller> type) {
        return new Predicate<ExtensionItemInfo>() {
            @Override
            public boolean apply(final @Nonnull ExtensionItemInfo input) {
                return input.getInstalledBy().contains(type);
            }
        };
    }
}
