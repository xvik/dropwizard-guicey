package ru.vyarus.dropwizard.guice.module.context.info.impl;

import com.google.common.collect.Sets;
import ru.vyarus.dropwizard.guice.module.context.ConfigItem;
import ru.vyarus.dropwizard.guice.module.context.ConfigScope;
import ru.vyarus.dropwizard.guice.module.context.info.BundleItemInfo;

import java.util.Set;

/**
 * Bundle item info implementation.
 *
 * @author Vyacheslav Rusakov
 * @since 06.07.2016
 */
public class BundleItemInfoImpl extends ItemInfoImpl implements BundleItemInfo {
    private final Set<Class<?>> disabledBy = Sets.newLinkedHashSet();

    public BundleItemInfoImpl(final Class<?> type) {
        super(ConfigItem.Bundle, type);
    }

    @Override
    public Set<Class<?>> getDisabledBy() {
        return disabledBy;
    }

    @Override
    public boolean isEnabled() {
        return disabledBy.isEmpty();
    }

    @Override
    public boolean isFromLookup() {
        return getRegisteredBy().contains(ConfigScope.BundleLookup.getType());
    }

    @Override
    public boolean isFromDwBundle() {
        return getRegisteredBy().contains(ConfigScope.DropwizardBundle.getType());
    }

    @Override
    public boolean isTransitive() {
        return getRegisteredBy().stream()
                .noneMatch(type -> ConfigScope.recognize(type) != ConfigScope.GuiceyBundle);
    }
}
