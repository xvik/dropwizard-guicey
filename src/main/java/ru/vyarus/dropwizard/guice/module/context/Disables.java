package ru.vyarus.dropwizard.guice.module.context;

import ru.vyarus.dropwizard.guice.module.context.info.ItemId;
import ru.vyarus.dropwizard.guice.module.context.info.ItemInfo;

import java.util.Arrays;
import java.util.function.Predicate;

/**
 * Common predicates used for items disabling. Use
 * {@link Predicate#and(Predicate)}, {@link Predicate#or(Predicate)} and {@link Predicate#negate()}
 * to build more complex predicates.
 *
 * @author Vyacheslav Rusakov
 * @since 09.04.2018
 */
public final class Disables {

    private Disables() {
    }

    /**
     * Shortcut for {@link #registeredBy(Class[])} for for special scopes (like classpath scan, bundles lookup etc).
     *
     * @param types special scopes
     * @return items registered in specific contexts predicate
     */
    public static Predicate<ItemInfo> registeredBy(final ConfigScope... types) {
        return registeredBy(Arrays.stream(types).map(ConfigScope::getType).toArray(Class<?>[]::new));
    }

    /**
     * Check registration source.  Context class could be
     * {@link io.dropwizard.Application}, {@link ru.vyarus.dropwizard.guice.module.installer.scanner.ClasspathScanner},
     * {@link ru.vyarus.dropwizard.guice.bundle.GuiceyBundleLookup} and classes implementing
     * {@link ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBundle}.
     *
     * @param types context class types
     * @return items registered in specific contexts predicate
     * @see ConfigScope for the list of all special scopes
     */
    public static Predicate<ItemInfo> registeredBy(final Class<?>... types) {
        // in time of disable predicate run registration scope == registered by
        return input -> Arrays.asList(types).contains(input.getRegistrationScope().getType());
    }

    public static Predicate<ItemInfo> registeredBy(final ItemId... scopes) {
        // in time of disable predicate run registration scope == registered by
        return input -> Arrays.asList(scopes).contains(input.getRegistrationScope());
    }

    /**
     * Generic item type predicate. It could be installer, bundle, extension or module.
     *
     * @param types configuration types to match
     * @return items of specific type predicate
     */
    public static Predicate<ItemInfo> itemType(final ConfigItem... types) {
        return Filters.type(types);
    }

    /**
     * @return extension item predicate
     */
    public static Predicate<ItemInfo> extension() {
        return itemType(ConfigItem.Extension);
    }

    /**
     * Note that only directly registered modules are covered.
     *
     * @return guice module item predicate
     */
    public static Predicate<ItemInfo> module() {
        return itemType(ConfigItem.Module);
    }

    /**
     * @return guicey bundle item predicate
     */
    public static Predicate<ItemInfo> bundle() {
        return itemType(ConfigItem.Bundle);
    }

    /**
     * @return installer item predicate
     */
    public static Predicate<ItemInfo> installer() {
        return itemType(ConfigItem.Installer);
    }

    /**
     * @param types target configuration item classes
     * @return exact configuration item types predicate
     */
    public static Predicate<ItemInfo> type(final Class<?>... types) {
        return input -> Arrays.asList(types).contains(input.getType());
    }

    /**
     * Match packages as "starts with": match all classes in package and subpackages.
     *
     * @param pkgs packages to match (at least one)
     * @return item type package matching predicate
     */
    public static Predicate<ItemInfo> inPackage(final String... pkgs) {
        return input -> {
            final String typePkg = input.getType().getPackage().getName();
            return Arrays.stream(pkgs).anyMatch(typePkg::startsWith);
        };
    }
}
