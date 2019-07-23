package ru.vyarus.dropwizard.guice.module.context.info.impl;

import com.google.common.collect.Sets;
import ru.vyarus.dropwizard.guice.module.context.ConfigItem;
import ru.vyarus.dropwizard.guice.module.context.ConfigScope;
import ru.vyarus.dropwizard.guice.module.context.info.ItemId;
import ru.vyarus.dropwizard.guice.module.context.info.ItemInfo;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Item info implementation.
 *
 * @author Vyacheslav Rusakov
 * @since 06.07.2016
 */
public class ItemInfoImpl implements ItemInfo {
    private final ItemId id;
    private final ConfigItem itemType;
    private final Set<ItemId> registeredBy = Sets.newLinkedHashSet();
    private ItemId registrationScope;
    private int registrationAttempts;
    // registrations per scope (actual + ignored)
    private final InstanceCounter counter = new InstanceCounter();

    public ItemInfoImpl(final ConfigItem itemType, final ItemId id) {
        this.itemType = itemType;
        this.id = id;
    }

    @Override
    public ItemId getId() {
        return id;
    }

    @Override
    public ConfigItem getItemType() {
        return itemType;
    }

    @Override
    public Class<?> getType() {
        return id.getType();
    }

    @Override
    public Set<ItemId> getRegisteredBy() {
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
        return getRegisteredBy().contains(ConfigScope.Application.getKey());
    }

    @Override
    public ItemId getRegistrationScope() {
        return registrationScope;
    }

    @Override
    public ConfigScope getRegistrationScopeType() {
        return ConfigScope.recognize(getRegistrationScope());
    }

    @Override
    public int getIgnoresByScope(final ItemId scope) {
        int res = counter.getCount(scope);
        if (scope.getIdentity() == null && res == 0) {
            // instance type check by class only case:
            // sum all registered instances of scope type
            for (ItemId reg : counter.getScopes()) {
                if (reg.getType().equals(scope.getType())) {
                    res += counter.getCount(reg);
                    // exclude actual registration from each item (only ignores counted)
                    if (reg.equals(registrationScope)) {
                        res--;
                    }
                }
            }
        } else {
            // exclude actual registration from overall attempts
            if (scope.equals(registrationScope)) {
                res--;
            }
        }
        return Math.max(res, 0);
    }

    @Override
    public int getIgnoresByScope(final Class<?> scope) {
        return getIgnoresByScope(ItemId.from(scope));
    }

    public void countRegistrationAttempt(final ItemId scope) {
        registrationAttempts++;
        if (registrationScope == null) {
            registrationScope = scope;
        }
        this.registeredBy.add(scope);
        this.counter.count(scope);
    }

    @Override
    public String toString() {
        return itemType.name() + " " + id;
    }

    /**
     * Counts instance appearances by scope.
     */
    private static class InstanceCounter {

        private final Map<ItemId, Integer> counts = new HashMap<>();

        public int count(final ItemId type) {
            final int value = getCount(type) + 1;
            counts.put(type, value);
            return value;
        }

        public int getCount(final ItemId type) {
            final Integer res = counts.get(type);
            return res == null ? 0 : res;
        }

        public Set<ItemId> getScopes() {
            return counts.keySet();
        }
    }
}
