package ru.vyarus.dropwizard.guice.module.context.info.impl;

import ru.vyarus.dropwizard.guice.module.context.ConfigItem;
import ru.vyarus.dropwizard.guice.module.context.info.ClassItemInfo;
import ru.vyarus.dropwizard.guice.module.context.info.ItemId;

/**
 * Class item info implementation.
 *
 * @author Vyacheslav Rusakov
 * @since 04.07.2019
 */
public abstract class ClassItemInfoImpl extends ItemInfoImpl implements ClassItemInfo {

    public ClassItemInfoImpl(final ConfigItem itemType, final Class<?> type) {
        super(itemType, ItemId.from(type));
    }
}
