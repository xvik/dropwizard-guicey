package ru.vyarus.dropwizard.guice.module.context.info.impl;

import ru.vyarus.dropwizard.guice.module.context.ConfigItem;
import ru.vyarus.dropwizard.guice.module.context.info.InstanceItemInfo;
import ru.vyarus.dropwizard.guice.module.context.info.ItemId;

import java.util.ArrayList;
import java.util.List;

/**
 * Instance item info implementation.
 *
 * @param <T> instance type
 * @author Vyacheslav Rusakov
 * @since 04.07.2019
 */
public abstract class InstanceItemInfoImpl<T> extends ItemInfoImpl implements InstanceItemInfo<T> {

    private final T instance;
    private int instanceCount;
    private final List<ItemId> duplicates = new ArrayList<>();

    /**
     * Create disable-only info (special constructor for disable-only items (without actual registration).
     *
     * @param itemType item type
     * @param type     item class
     */
    public InstanceItemInfoImpl(final ConfigItem itemType, final Class type) {
        super(itemType, ItemId.from(type));
        this.instance = null;
    }

    /**
     * Create item.
     *
     * @param itemType item type
     * @param instance item instance
     */
    public InstanceItemInfoImpl(final ConfigItem itemType, final T instance) {
        super(itemType, ItemId.from(instance));
        this.instance = instance;
    }

    @Override
    public T getInstance() {
        return instance;
    }

    @Override
    public int getInstanceCount() {
        return instanceCount;
    }

    /**
     * @param instanceCount instances count
     */
    public void setInstanceCount(final int instanceCount) {
        this.instanceCount = instanceCount;
    }

    @Override
    public List<ItemId> getDuplicates() {
        return duplicates;
    }

    @Override
    public String toString() {
        return super.toString() + (getInstanceCount() > 0 ? " (#" + getInstanceCount() + ")" : "");
    }
}
