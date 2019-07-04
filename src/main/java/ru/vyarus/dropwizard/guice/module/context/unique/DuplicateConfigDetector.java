package ru.vyarus.dropwizard.guice.module.context.unique;

import ru.vyarus.dropwizard.guice.module.context.info.ItemInfo;

import java.util.Collection;

/**
 * Configuration items may be registered by class (extension, installer) or by instance (guicey bundle, guice module).
 * Duplicates are obvious for class registrations: one class - one extension instance (no matter how much times
 * this class was registered). But for instances it's not so obvious. For example, in dropwizard you can
 * register multiple instances of the same bundle (and it's often very useful!). But, sometimes,
 * there are common bundles (or guice modules), used by multiple other bundles (and so declared by all of them).
 * In this case it would be desirable to register common bundle just once (and avoid duplicate).
 * <p>
 * This is exactly the case why duplicates detector exists: when multiple instances of the same class detected
 * (guicey bundle, guice module) detector is asked to decide if multiple instances are allowed.
 * Custom implementations may be used to resolve existing duplicates (e.g. registered by different 3rd party modules).
 *
 * @author Vyacheslav Rusakov
 * @see EqualDuplicatesDetector as default implementation
 * @see LegacyModeDuplicatesDetector with legacy guicey behaviour implementation (always one instance per class)
 * @since 03.07.2019
 */
public interface DuplicateConfigDetector {

    /**
     * Called every time when configured object (guicey bundle or guice module) of the same type is already registered.
     * Used to resolve duplicate registrations: decide to allow another instance or not.
     * <p>
     * If 3 or more instances of the same type are registered then method would be called for each new configured
     * instance (in case of 3 registrations - 2 method calls).
     *
     * @param info       item descriptor
     * @param registered already registered items
     * @param newItem    new item to check
     * @return true to prevent item registration, false to allow registration
     */
    boolean isDuplicate(ItemInfo info, Collection<Object> registered, Object newItem);
}
