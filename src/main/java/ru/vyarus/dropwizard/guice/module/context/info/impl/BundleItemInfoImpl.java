package ru.vyarus.dropwizard.guice.module.context.info.impl;

import com.google.common.collect.Sets;
import ru.vyarus.dropwizard.guice.module.context.ConfigItem;
import ru.vyarus.dropwizard.guice.module.context.ConfigScope;
import ru.vyarus.dropwizard.guice.module.context.info.BundleItemInfo;
import ru.vyarus.dropwizard.guice.module.context.info.ItemId;
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBundle;

import java.util.Set;

/**
 * Bundle item info implementation.
 *
 * @author Vyacheslav Rusakov
 * @since 06.07.2016
 */
public class BundleItemInfoImpl extends InstanceItemInfoImpl implements BundleItemInfo {
    private final Set<ItemId> disabledBy = Sets.newLinkedHashSet();

    // disable only
    public BundleItemInfoImpl(final Class<? extends GuiceyBundle> type) {
        super(ConfigItem.Bundle, type);
    }

    public BundleItemInfoImpl(final GuiceyBundle bundle) {
        super(ConfigItem.Bundle, bundle);
    }

    @Override
    public Set<ItemId> getDisabledBy() {
        return disabledBy;
    }

    @Override
    public boolean isEnabled() {
        return disabledBy.isEmpty();
    }

    @Override
    public boolean isFromLookup() {
        return getRegisteredBy().contains(ConfigScope.BundleLookup.getKey());
    }

    @Override
    public boolean isTransitive() {
        return getRegisteredBy().stream()
                .noneMatch(type -> ConfigScope.recognize(type) != ConfigScope.GuiceyBundle);
    }
}
