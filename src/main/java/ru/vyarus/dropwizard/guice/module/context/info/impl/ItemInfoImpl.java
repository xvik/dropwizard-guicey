package ru.vyarus.dropwizard.guice.module.context.info.impl;

import ru.vyarus.dropwizard.guice.module.context.ConfigItem;
import ru.vyarus.dropwizard.guice.module.context.ConfigScope;
import ru.vyarus.dropwizard.guice.module.context.info.ItemInfo;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Item info implementation.
 *
 * @author Vyacheslav Rusakov
 * @since 06.07.2016
 */
public class ItemInfoImpl implements ItemInfo {
    private final ConfigItem itemType;
    private final Class<?> type;
    private final Set<Class<?>> registeredBy = new LinkedHashSet<>();
    private final List<Class<?>> registrationScopes = new ArrayList<>();
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
    public List<Class<?>> getRegistrationScopes() {
        return registrationScopes;
    }

    @Override
    public List<ConfigScope> getRegistrationScopeTypes() {
        return getRegistrationScopes().stream().map(ConfigScope::recognize).collect(Collectors.toList());
    }

    public void countRegistrationAttempt() {
        registrationAttempts++;
    }

    public void addRegistrationScope(final Class<?> registrationScope) {
        this.registrationScopes.add(registrationScope);
    }

    @Override
    public String toString() {
        return itemType.name() + " " + type.getSimpleName();
    }
}
