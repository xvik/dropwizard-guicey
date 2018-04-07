package ru.vyarus.dropwizard.guice.module.context.info.impl;

import com.google.common.collect.Sets;
import ru.vyarus.dropwizard.guice.module.context.ConfigItem;
import ru.vyarus.dropwizard.guice.module.context.info.ModuleItemInfo;

import java.util.Set;

/**
 * Module item info implementation.
 *
 * @author Vyacheslav Rusakov
 * @since 03.04.2018
 */
public class ModuleItemInfoImpl extends ItemInfoImpl implements ModuleItemInfo {

    private final Set<Class<?>> disabledBy = Sets.newLinkedHashSet();

    public ModuleItemInfoImpl(final Class<?> type) {
        super(ConfigItem.Module, type);
    }

    @Override
    public Set<Class<?>> getDisabledBy() {
        return disabledBy;
    }

    @Override
    public boolean isEnabled() {
        return disabledBy.isEmpty();
    }
}
