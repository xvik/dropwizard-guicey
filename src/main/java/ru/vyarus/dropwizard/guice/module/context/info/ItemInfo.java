package ru.vyarus.dropwizard.guice.module.context.info;

import ru.vyarus.dropwizard.guice.module.context.ConfigItem;
import ru.vyarus.dropwizard.guice.module.context.ConfigScope;

import java.util.List;
import java.util.Set;

/**
 * Base interface for instance item info objects. Combines common signs for all configuration items.
 * Items may be registered by application (direct registration through {@link ru.vyarus.dropwizard.guice.GuiceBundle},
 * by classpath scan or by {@link ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBundle}.
 * <p>
 * Multiple sources may register single item and even the same source could register it multiple times.
 * Each entity will be actually registered once, but information about all registrations is stored and may be used for
 * warnings reporting.
 *
 * @author Vyacheslav Rusakov
 * @see ConfigItem for list of available items
 * @since 09.07.2016
 */
public interface ItemInfo {

    /**
     * @return configuration item type (e.g. installer, bundle, extension etc)
     */
    ConfigItem getItemType();

    /**
     * @return actual item class
     */
    Class<?> getType();

    /**
     * Configuration items may be registered by root application class, classpath scan or guicey bundle.
     * For registrations in application class {@link io.dropwizard.Application} is stored as context.
     * For registration by classpath scan {@link ru.vyarus.dropwizard.guice.module.installer.scanner.ClasspathScanner}
     * is stored as context. For registrations by
     * {@link ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBundle}, actual bundle class is stored
     * as context.
     * Bundle items may also have {@link ru.vyarus.dropwizard.guice.bundle.GuiceyBundleLookup} as context classe for
     * bundles resolved by lookup mechanism.
     * <p>
     * May not contain elements if item was never registered, but for example, disabled.
     *
     * @return context classes which register item or empty collection
     * @see ConfigScope for the list of all special scopes
     */
    Set<Class<?>> getRegisteredBy();

    /**
     * Item may be registered multiple times. For class items (e.g. extension) only first scope will be actual
     * registration scope (and registrations from other scopes will be simply ignored), but instance items
     * (bundle, module) may be registered in multiple scopes (note that duplicates detection for instances is
     * implemented by {@link ru.vyarus.dropwizard.guice.module.context.unique.DuplicateConfigDetector}).
     * <p>
     * May be empty list for never registered but disabled items!
     *
     * @return registration scope
     * @see #getRegisteredBy() for all scopes performing registratoin
     */
    List<Class<?>> getRegistrationScopes();

    /**
     * It is essentially the same as {@link #getRegistrationScopes()}, but with generified guicey bundle scope.
     * May be useful for generic reporting.
     * <p>
     * Note that only instance items (bundle, module) may contain multiple scopes, class items (extensions, installer)
     * will always have only one scope (or no scope at all if item was disabled but not registered).
     *
     * @return list of registration scopes types or empty list
     */
    List<ConfigScope> getRegistrationScopeTypes();

    /**
     * It may be 0 for disabled items (e.g. installer disabled but never registered).
     * Also, count may be greater than registration sources count, because the same source could register
     * item multiple times.
     *
     * @return count of item registrations
     */
    int getRegistrationAttempts();

    /**
     * Useful to recognize not registered info items appeared for example because of item disabling.
     *
     * @return true if item was registered, false otherwise
     */
    boolean isRegistered();

    /**
     * @return true if item directly registered through {@link ru.vyarus.dropwizard.guice.GuiceBundle.Builder},
     * false otherwise.
     */
    boolean isRegisteredDirectly();
}
