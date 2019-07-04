package ru.vyarus.dropwizard.guice.module.context.info.impl;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import ru.vyarus.dropwizard.guice.module.context.ConfigItem;
import ru.vyarus.dropwizard.guice.module.context.info.InstanceItemInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Instance item info implementation.
 *
 * @author Vyacheslav Rusakov
 * @since 04.07.2019
 */
public class InstanceItemInfoImpl extends ItemInfoImpl implements InstanceItemInfo {

    private int registrations;
    private final Multimap<Class<?>, Object> registered = LinkedHashMultimap.create();
    private final Multimap<Class<?>, Object> duplicates = LinkedHashMultimap.create();

    public InstanceItemInfoImpl(final ConfigItem itemType, final Class<?> type) {
        super(itemType, type);
    }

    @Override
    public List<Object> getRegistrationsByScope(final Class<?> scope) {
        return new ArrayList<>(registered.get(scope));
    }

    @Override
    public List<Object> getDuplicatesByScope(final Class<?> scope) {
        return new ArrayList<>(duplicates.get(scope));
    }

    @Override
    public int getRegistrations() {
        return registrations;
    }

    public void addRegisteredInstance(final Class<?> scope, final Object instance) {
        registered.put(scope, instance);
        registrations++;
    }

    public void addDuplicateInstance(final Class<?> scope, final Object instance) {
        duplicates.put(scope, instance);
    }
}
