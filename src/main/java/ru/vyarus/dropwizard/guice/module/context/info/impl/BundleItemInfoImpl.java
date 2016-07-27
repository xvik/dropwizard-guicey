package ru.vyarus.dropwizard.guice.module.context.info.impl;

import io.dropwizard.Bundle;
import ru.vyarus.dropwizard.guice.bundle.GuiceyBundleLookup;
import ru.vyarus.dropwizard.guice.module.context.ConfigItem;
import ru.vyarus.dropwizard.guice.module.context.info.BundleItemInfo;

/**
 * Bundle item info implementation.
 *
 * @author Vyacheslav Rusakov
 * @since 06.07.2016
 */
public class BundleItemInfoImpl extends ItemInfoImpl implements BundleItemInfo {

    public BundleItemInfoImpl(final Class<?> type) {
        super(ConfigItem.Bundle, type);
    }

    @Override
    public boolean isFromLookup() {
        return getRegisteredBy().contains(GuiceyBundleLookup.class);
    }

    @Override
    public boolean isFromDwBundle() {
        return getRegisteredBy().contains(Bundle.class);
    }
}
