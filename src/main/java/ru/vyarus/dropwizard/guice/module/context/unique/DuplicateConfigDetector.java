package ru.vyarus.dropwizard.guice.module.context.unique;

import java.util.List;

/**
 * Configuration items may be registered by class (extension, installer) or by instance (guicey bundle, guice module).
 * Duplicates are obvious for class registrations: one class - one extension instance (no matter how much times
 * this class was registered). But for instances it's not so obvious. For example, in dropwizard you can
 * register multiple instances of the same bundle (and it's often very useful!). But, sometimes,
 * there are common bundles (or guice modules), used by multiple other bundles (and so declared by all of them).
 * In this case it would be desirable to register common bundle just once (and avoid duplicate).
 * <p>
 * Default deduplication logic for instances will consider equal objects as duplicate. In cases, when it is impossible
 * to properly implement equals method (e.g. 3rd party class), custom deduplication detector could be implemented.
 *
 * @author Vyacheslav Rusakov
 * @see LegacyModeDuplicatesDetector with legacy guicey behaviour implementation (always one instance per class)
 * @since 03.07.2019
 */
public interface DuplicateConfigDetector {

    /**
     * Called every time when configured object (guicey bundle or guice module) of the same type is already registered.
     * Note that method is called only if new instance is not equal to any registered instances of the same type.
     * Method must return duplicate object from already registered instances (or null to allow new registration).
     * <p>
     * For example, if 3 or more instances of the same type are registered then method would be called for each new
     * configured instance (in case of 3 registrations - 2 method calls, assuming that instances are not equal).
     * <p>
     * Provided instances will always be of the same class (no polymorphic checks). Classes loaded from different
     * class loaders will be recognized as same class (and so provided instances may be instances of the same class
     * but from different class loaders).
     *
     * @param registered already registered items
     * @param newItem    new item to check
     * @return duplicate object from already registered (to mark this one as duplicate to already registered)
     */
    Object getDuplicateItem(List<Object> registered, Object newItem);
}
