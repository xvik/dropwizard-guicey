package ru.vyarus.dropwizard.guice.module.context.info.impl;

import com.google.common.collect.Sets;
import ru.vyarus.dropwizard.guice.module.context.ConfigItem;
import ru.vyarus.dropwizard.guice.module.context.info.BundleItemInfo;
import ru.vyarus.dropwizard.guice.module.context.info.ItemId;
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBundle;

import java.util.Set;

/**
 * Bundle item info generic implementation.
 *
 * @author Vyacheslav Rusakov
 * @since 28.07.2019
 */
public abstract class BundleItemInfoImpl extends InstanceItemInfoImpl implements BundleItemInfo {

    private final Set<ItemId> disabledBy = Sets.newLinkedHashSet();

    // disable only
    public BundleItemInfoImpl(final ConfigItem type, final Class<? extends GuiceyBundle> item) {
        super(type, item);
    }

    public BundleItemInfoImpl(final ConfigItem type, final Object instance) {
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
