package ru.vyarus.dropwizard.guice.module.context.info.impl;

import com.google.common.collect.Sets;
import ru.vyarus.dropwizard.guice.module.context.ConfigItem;
import ru.vyarus.dropwizard.guice.module.context.info.BundleItemInfo;
import ru.vyarus.dropwizard.guice.module.context.info.ItemId;

import java.util.Set;

/**
 * Bundle item info generic implementation.
 *
 * @param <T> instance type
 * @author Vyacheslav Rusakov
 * @since 28.07.2019
 */
public abstract class BundleItemInfoImpl<T> extends InstanceItemInfoImpl<T> implements BundleItemInfo<T> {

    private final Set<ItemId> disabledBy = Sets.newLinkedHashSet();

    // disable only
    public BundleItemInfoImpl(final ConfigItem type, final Class<?> item) {
        super(type, item);
    }

    public BundleItemInfoImpl(final ConfigItem type, final T instance) {
        super(type, instance);
    }

    @Override
    public Set<ItemId> getDisabledBy() {
        return disabledBy;
    }

    @Override
    public boolean isEnabled() {
        return disabledBy.isEmpty();
    }
}
