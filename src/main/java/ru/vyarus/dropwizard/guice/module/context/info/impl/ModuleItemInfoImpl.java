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

    private static ThreadLocal<Boolean> override = new ThreadLocal<>();

    private final Set<Class<?>> disabledBy = Sets.newLinkedHashSet();
    private final boolean overriding;

    public ModuleItemInfoImpl(final Class<?> type) {
        super(ConfigItem.Module, type);
        this.overriding = override.get() != null;
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
