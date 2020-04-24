package ru.vyarus.dropwizard.guice.module.context.info;

import java.util.List;

/**
 * Base interface for instance configurations (bundles, modules). Multiple instances of the same type could be
 * actually registered and different objects will have different item info objects. Only direct registrations
 * of the same instance or equal objects will be considered as duplicate (second registration attempt will be just
 * registered in existing item info).
 *
 * @author Vyacheslav Rusakov
 * @param <T> contained instance type
 * @since 03.07.2019
 */
public interface InstanceItemInfo<T> extends ItemInfo {

    /**
     * @return configuration object instance
     */
    T getInstance();

    /**
     * For example, if multiple bundles registered: {@code .bundles(new Bundle(), new Bundle(), new Bundle()}
     * then their counts would be 1, 2 and 3 in order of registration. For the same instances counts will be the same.
     * This number is required to differentiate instances in reporting.
     *
     * @return instance registration count number (starting from 0)
     */
    int getInstanceCount();

    /**
     * Duplicate instances are completely ignored. Information is provided for diagnostic.
     * @return list of detected duplicate instances for current
     */
    List<ItemId> getDuplicates();
}
