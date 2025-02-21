package ru.vyarus.dropwizard.guice.module.context.info;

import ru.vyarus.dropwizard.guice.module.context.ConfigItem;
import ru.vyarus.dropwizard.guice.module.context.ConfigScope;

import java.util.Set;

/**
 * Base interface for item info objects. Combines common signs for all configuration items.
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
     * Items could be registered by class and by instance. In case of instance registration, multiple instances
     * could be provided with the same class. For class registrations item id is equal to pure class.
     *
     * @return item identity
     */
    ItemId getId();

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
     * For registrations in application class {@link io.dropwizard.core.Application} is stored as context.
     * For registration by classpath scan {@link ru.vyarus.dropwizard.guice.module.installer.scanner.ClasspathScanner}
     * is stored as context. For registrations by
     * {@link ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBundle}, actual bundle class is stored
     * as context.
     * Bundle items may also have {@link ru.vyarus.dropwizard.guice.bundle.GuiceyBundleLookup} as context classe for
     * bundles resolved by lookup mechanism.
     * <p>
     * May not contain elements if item was never registered, but for example, disabled.
     * <p>
     * To quick check if exact scope class is present use {@code ItemId.from(class)}, which will match
     * any class instance related scope key.
     *
     * @return context classes which register item or empty collection
     * @see ConfigScope for the list of all special scopes
     */
    Set<ItemId> getRegisteredBy();

    /**
     * Item may be registered multiple times. For class items (e.g. extension) only first scope will be actual
     * registration scope (and registrations from other scopes will be simply ignored). For instance items
     * (bundle, module), different objects of the same type will be registered according to
     * {@link ru.vyarus.dropwizard.guice.module.context.unique.DuplicateConfigDetector}).
     * <p>
     * May be null for never registered but disabled items!
     *
     * @return registration scope
     * @see #getRegisteredBy() for all scopes performing registratoin
     */
    ItemId getRegistrationScope();

    /**
     * It is essentially the same as {@link #getRegistrationScope()}, but with generified guicey bundle scope.
     * May be useful for generic reporting.
     *
     * @return type of registration scope
     */
    ConfigScope getRegistrationScopeType();

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

    /**
     * Required to show ignored items in the same scope as actual registration.
     * <p>
     * When checked bundles by class only item ({@code ItemId.from(Bundle.class)}) it wil return sum
     * of registrations for all instances of type.
     *
     * @param scope scope
     * @return number of ignored items in scope
     */
    int getIgnoresByScope(ItemId scope);

    /**
     * Shortcut for {@link #getIgnoresByScope(ItemId)}.
     *
     * @param scope scope type to check ignores
     * @return number of ignored items in all scopes of specified type
     */
    int getIgnoresByScope(Class<?> scope);

    /**
     * For example, item for extension might be registered before recognition with installer, and it is
     * important to wait for installer before applying disable predicates (which potentially may rely on installer).
     *
     * @return true if item completely initialized (all related data provided)
     */
    boolean isAllDataCollected();
}
