package ru.vyarus.dropwizard.guice.module.context;

import com.google.common.base.Preconditions;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import ru.vyarus.dropwizard.guice.hook.GuiceyConfigurationHook;
import ru.vyarus.dropwizard.guice.module.context.info.ItemId;
import ru.vyarus.dropwizard.guice.module.context.info.ItemInfo;
import ru.vyarus.dropwizard.guice.module.context.info.impl.ItemInfoImpl;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Public api for collected guicey configuration info. In contrast to {@link ConfigurationContext},
 * used during configuration, contains only configuration item ids (item ids used instead of pure classes
 * because multiple instances of the same type could be registered (e.g. multiple bundles or modules)).
 * <p>
 * Configuration info may be used for any kind of diagnostics: configuration logging, configuration tree rendering,
 * automatic configuration warnings generation etc.
 * <p>
 * Configuration items are stored in registration order. Information querying is implemented with help of java 8
 * {@link java.util.function.Predicate}.
 * <p>
 * Available for direct injection, but prefer accessing from
 * {@link ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo#getData()}.
 *
 * @author Vyacheslav Rusakov
 * @see ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo as high level info api
 * @see ConfigItem for the list of available confiugration items
 * @since 06.07.2016
 */
public final class ConfigurationInfo {

    // NOTE: ItemId for instance is equal to ItemId of class, but there would always be instance ItemIds
    // if something was registered and class ItemId if item was disabled but never queried

    // required structure to preserve registration order
    private final Multimap<ConfigItem, ItemId> itemsHolder = LinkedHashMultimap.create();
    // preserve all class types + pure disable (without registrations) of instance types
    private final Map<ItemId, ItemInfo> classTypes = Maps.newHashMap();
    // preserve all instance types together
    private final Multimap<Class<?>, ItemInfo> instanceTypes = LinkedHashMultimap.create();

    private final List<Class<? extends GuiceyConfigurationHook>> hooks;

    /**
     * Create configuration info.
     *
     * @param context configuration context
     */
    public ConfigurationInfo(final ConfigurationContext context) {
        hooks = context.getExecutedHookTypes();
        // convert all objects into types (more suitable for analysis)
        for (ConfigItem type : ConfigItem.values()) {
            for (Object item : context.getItems(type)) {
                final ItemId id = ItemId.from(item);
                itemsHolder.put(type, id);
                final ItemInfoImpl info = context.getInfo(item);

                // put instance item pure disable descriptor into classTypes
                if (info.getItemType().isInstanceConfig() && info.getRegistrationAttempts() > 0) {
                    instanceTypes.put(info.getType(), info);
                } else {
                    classTypes.put(id, info);
                }
            }
        }
    }

    /**
     * Pay attention that disabled (or disabled and never registered) items are also returned.
     *
     * @param type configuration item type
     * @param <T>  expected class
     * @return registered item ids of required type in registration order or empty list if nothing registered
     */
    @SuppressWarnings("unchecked")
    public <T> List<ItemId<T>> getItems(final ConfigItem type) {
        final Collection res = itemsHolder.get(type);
        return res.isEmpty() ? Collections.emptyList() : new ArrayList<>(res);
    }

    /**
     * Used to query items of one configuration type (e.g. only installers or bundles). Some common filters are
     * predefined in {@link Filters}. Use {@link Predicate#and(Predicate)}, {@link Predicate#or(Predicate)}
     * and {@link Predicate#negate()} to reuse default filters.
     * <p>
     * Pay attention that disabled (or disabled and never registered) items are also returned.
     *
     * @param type   configuration item type
     * @param filter predicate to filter definitions
     * @param <T>    expected class
     * @param <K>    expected info container class
     * @return registered item ids in registration order, filtered with provided filter or empty list
     */
    public <T, K extends ItemInfo> List<ItemId<T>> getItems(final ConfigItem type, final Predicate<K> filter) {
        final List<ItemId<T>> items = getItems(type);
        return filter(items, filter);
    }

    /**
     * Used to query items of all configuration types. May be useful to build configuration tree (e.g. to search
     * all items configured by bundle or by classpath scan). Some common filters are
     * predefined in {@link Filters}. Use {@link Predicate#and(Predicate)}, {@link Predicate#or(Predicate)}
     * and {@link Predicate#negate()} to reuse default filters.
     * <p>
     * Pay attention that disabled (or disabled and never registered) items are also returned.
     *
     * @param filter predicate to filter definitions
     * @return registered item ids in registration order, filtered with provided filter or empty list
     */
    @SuppressWarnings("unchecked")
    public List<ItemId<Object>> getItems(final Predicate<? extends ItemInfo> filter) {
        final List<ItemId<Object>> items = new ArrayList(itemsHolder.values());
        return filter(items, filter);
    }

    /**
     * @param type item type
     * @return registered items by type (possibly multiple results for instance item)
     */
    @SuppressWarnings("unchecked")
    public List<ItemId<Object>> getItems(final Class<?> type) {
        final List<ItemInfo> instances = getInfos(type);
        if (instances.isEmpty()) {
            return Collections.emptyList();
        }

        final List<ItemId<Object>> res = new ArrayList<>();
        for (ItemInfo item : instances) {
            res.add(item.getId());
        }
        return res;
    }

    /**
     * NOTE: it will not return first instance of type if called as {@code getInfo(ItemId.from(Bundle.class)))}!
     * It could only return non null object for instance type if exact instance identity provided or
     * disable - only info (when item was disabled but never registered then calling with id
     * {@code ItemId.from(Bundle.class)} will return disable item info (information that all instances of class
     * are disabled)). But note, that general disable into is not created if at least one instance was registered.
     *
     * @param id  configured item id
     * @param <T> expected configuration container type
     * @return item configuration info or null if item not registered
     * @see #getInfos(Class) to retrieve all instace items of type
     */
    @SuppressWarnings("unchecked")
    public <T extends ItemInfo> T getInfo(final ItemId id) {
        if (id.getIdentity() == null) {
            // check if trying to reach instance descriptor by class identity
            final int instancesCnt = instanceTypes.get(id.getType()).size();
            Preconditions.checkState(instancesCnt == 0,
                    "Class id descriptor (%s) can't be used to reach instance configurations: %s. "
                            + "Use getInfos(class) instead.",
                    id, instancesCnt);
        }

        // searching instance
        final boolean instanceType = id.getIdentity() != null;
        if (instanceType) {
            for (ItemInfo info : instanceTypes.get(id.getType())) {
                if (info.getId().equals(id)) {
                    return (T) info;
                }
            }
        }

        return instanceType ? null : (T) classTypes.get(id);
    }

    /**
     * Returns all registrations of type. For class based extensions will return list of one element (the same result
     * as {@code getInfo(ItemId.from(type))}).For instance types will return all registered instances of type.
     *
     * @param type item class
     * @param <T>  item info type
     * @return all registrations of type or empty list if nothing registered
     */
    @SuppressWarnings("unchecked")
    public <T extends ItemInfo> List<T> getInfos(final Class<?> type) {
        final ItemId id = ItemId.from(type);
        // it may be request for class item or for all instance items (of provided class)
        if (instanceTypes.containsKey(id.getType())) {
            return new ArrayList<T>((Collection<T>) instanceTypes.get(id.getType()));
        }
        // if item is only disabled without actual registration
        final T res = (T) classTypes.get(id);
        return res == null ? Collections.emptyList() : Collections.singletonList(res);
    }

    /**
     * The simple way to receive a large set of info objects, instead of just ids. Useful for sorting.
     *
     * @param type   required item type
     * @param filter filter
     * @param <T>    item type
     * @return all registrations matching filter or empty list if nothing registered
     */
    public <T extends ItemInfo> List<T> getInfos(final ConfigItem type, final Predicate<T> filter) {
        return filterInfos(getItems(type), filter);
    }

    /**
     * @return types of executed hooks
     */
    public List<Class<? extends GuiceyConfigurationHook>> getHooks() {
        return hooks;
    }

    private <T, K extends ItemInfo> List<ItemId<T>> filter(final List<ItemId<T>> items, final Predicate<K> filter) {
        return items.stream().filter(it -> filter.test(getInfo(it))).collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    private <K extends ItemInfo> List<K> filterInfos(final List<ItemId<Object>> items, final Predicate<K> filter) {
        return items.stream()
                .map(itemId -> (K) getInfo(itemId))
                .filter(filter).collect(Collectors.toList());
    }
}
