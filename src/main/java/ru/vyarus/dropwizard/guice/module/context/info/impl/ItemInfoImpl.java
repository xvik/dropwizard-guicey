package ru.vyarus.dropwizard.guice.module.context.info.impl;

import com.google.common.collect.Sets;
import ru.vyarus.dropwizard.guice.module.context.ConfigItem;
import ru.vyarus.dropwizard.guice.module.context.ConfigScope;
import ru.vyarus.dropwizard.guice.module.context.info.ItemInfo;

import java.util.Set;

/**
 * Item info implementation.
 *
 * @author Vyacheslav Rusakov
 * @since 06.07.2016
 */
public class ItemInfoImpl implements ItemInfo {
    private final ConfigItem itemType;
    private final Class<?> type;
    private final Set<Class<?>> registeredBy = Sets.newLinkedHashSet();
    private Class<?> registrationScope;
    private int registrationAttempts;

    public ItemInfoImpl(final ConfigItem itemType, final Class<?> type) {
        this.itemType = itemType;
        this.type = type;
    }

    @Override
    public ConfigItem getItemType() {
        return itemType;
    }

    @Override
    public Class<?> getType() {
        return type;
    }

    @Override
    public Set<Class<?>> getRegisteredBy() {
        return registeredBy;
    }

    @Override
    public int getRegistrationAttempts() {
        return registrationAttempts;
    }

    @Override
    public boolean isRegistered() {
        return !registeredBy.isEmpty();
    }

    @Override
    public boolean isRegisteredDirectly() {
        return getRegisteredBy().contains(ConfigScope.Application.getType());
    }

    @Override
    public Class<?> getRegistrationScope() {
        return registrationScope;
    }

    @Override
    public ConfigScope getRegistrationScopeType() {
        return ConfigScope.recognize(getRegistrationScope());
    }

    public void countRegistrationAttempt() {
        registrationAttempts++;
    }

    public void setRegistrationScope(final Class<?> registrationScope) {
        this.registrationScope = registrationScope;
    }

    @Override
    public String toString() {
        return itemType.name() + " " + type.getSimpleName();
    }
}
