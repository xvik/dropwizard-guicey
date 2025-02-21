package ru.vyarus.dropwizard.guice.module.context;

import ru.vyarus.dropwizard.guice.module.context.info.DropwizardBundleItemInfo;
import ru.vyarus.dropwizard.guice.module.context.info.ExtensionItemInfo;
import ru.vyarus.dropwizard.guice.module.context.info.GuiceyBundleItemInfo;
import ru.vyarus.dropwizard.guice.module.context.info.InstallerItemInfo;
import ru.vyarus.dropwizard.guice.module.context.info.ItemId;
import ru.vyarus.dropwizard.guice.module.context.info.ItemInfo;
import ru.vyarus.dropwizard.guice.module.context.info.ModuleItemInfo;

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
     * {@link io.dropwizard.core.Application},
     * {@link ru.vyarus.dropwizard.guice.module.installer.scanner.ClasspathScanner},
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
     * @return extension item predicate
     */
    public static Predicate<ItemInfo> extension(final Predicate<ExtensionItemInfo> predicate) {
        return extension().and(item -> predicate.test((ExtensionItemInfo) item));
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
     * Note that only directly registered modules are covered.
     *
     * @return guice module item predicate
     */
    public static Predicate<ItemInfo> module(final Predicate<ModuleItemInfo> predicate) {
        return module().and(item -> predicate.test((ModuleItemInfo) item));
    }

    /**
     * @return guicey bundle item predicate
     */
    public static Predicate<ItemInfo> bundle() {
        return itemType(ConfigItem.Bundle);
    }

    /**
     * @return guicey bundle item predicate
     */
    public static Predicate<ItemInfo> bundle(final Predicate<GuiceyBundleItemInfo> predicate) {
        return bundle().and(item -> predicate.test((GuiceyBundleItemInfo) item));
    }

    /**
     * Note that only directly registered dropwizard bundles are covered.
     *
     * @return guicey dropwizard bundle item predicate
     */
    public static Predicate<ItemInfo> dropwizardBundle() {
        return itemType(ConfigItem.DropwizardBundle);
    }

    /**
     * Note that only directly registered dropwizard bundles are covered.
     *
     * @return guicey dropwizard bundle item predicate
     */
    public static Predicate<ItemInfo> dropwizardBundle(final Predicate<DropwizardBundleItemInfo> predicate) {
        return dropwizardBundle().and(item -> predicate.test((DropwizardBundleItemInfo) item));
    }

    /**
     * @return installer item predicate
     */
    public static Predicate<ItemInfo> installer() {
        return itemType(ConfigItem.Installer);
    }

    /**
     * @return installer item predicate
     */
    public static Predicate<ItemInfo> installer(final Predicate<InstallerItemInfo> predicate) {
        return installer().and(item -> predicate.test((InstallerItemInfo) item));
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

    /**
     * Web extensions are all extensions activated with jetty (including jersey extensions like rest resources).
     * (web extensions identified by installers, implementing
     * {@link ru.vyarus.dropwizard.guice.module.installer.install.WebInstaller})
     *
     * @return web extensions predicate
     */
    public static Predicate<ItemInfo> webExtension() {
        return extension().and(item -> ((ExtensionItemInfo) item).isWebExtension());
    }

    /**
     * Jersey extensions are extensions installed with
     * {@link ru.vyarus.dropwizard.guice.module.installer.install.JerseyInstaller}.
     *
     * @return jersey extensions predicate
     */
    public static Predicate<ItemInfo> jerseyExtension() {
        return extension().and(item -> ((ExtensionItemInfo) item).isJerseyExtension());
    }
}
