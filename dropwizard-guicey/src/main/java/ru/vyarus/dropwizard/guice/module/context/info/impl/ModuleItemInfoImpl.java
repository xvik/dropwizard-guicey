package ru.vyarus.dropwizard.guice.module.context.info.impl;

import com.google.common.collect.Sets;
import com.google.inject.Module;
import ru.vyarus.dropwizard.guice.module.context.ConfigItem;
import ru.vyarus.dropwizard.guice.module.context.info.ItemId;
import ru.vyarus.dropwizard.guice.module.context.info.ModuleItemInfo;

import java.util.Set;

/**
 * Module item info implementation.
 *
 * @author Vyacheslav Rusakov
 * @since 03.04.2018
 */
public class ModuleItemInfoImpl extends InstanceItemInfoImpl<Module> implements ModuleItemInfo {

    private static ThreadLocal<Boolean> override = new ThreadLocal<>();

    private final Set<ItemId> disabledBy = Sets.newLinkedHashSet();
    private final boolean overriding;

    /**
     * Create disabled-only item (without an actual item).
     *
     * @param type item type
     */
    public ModuleItemInfoImpl(final Class<? extends Module> type) {
        super(ConfigItem.Module, type);
        this.overriding = false;
    }

    /**
     * Create item.
     *
     * @param module module instance
     */
    public ModuleItemInfoImpl(final Module module) {
        super(ConfigItem.Module, module);
        this.overriding = override.get() != null;
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
    public boolean isOverriding() {
        return overriding;
    }

    /**
     * Use to register overriding modules. Such complex approach was used because overriding modules
     * is the only item that require additional parameter during registration. This parameter may be used
     * in disable predicate to differentiate overriding modules from normal modules.
     *
     * @param action registration action
     */
    public static void overrideScope(final Runnable action) {
        override.set(true);
        try {
            action.run();
        } finally {
            override.remove();
        }
    }
}
