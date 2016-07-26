package ru.vyarus.dropwizard.guice.module.context;

import com.google.common.base.Predicate;
import com.google.common.collect.*;
import ru.vyarus.dropwizard.guice.module.context.info.ItemInfo;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Public api for collected guicey configuration info. In contrast to {@link ConfigurationContext},
 * used during configuration, contains only configuration item classes.
 * <p>
 * Configuration info may be used for any kind of diagnostics: configuration logging, configuration tree rendering,
 * automatic configuration warnings generation etc.
 * <p>
 * Configuration items are stored in registration order. Information querying is implemented with help of guava
 * {@link Predicate}.
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

    // required structure to preserve registration order
    private final Multimap<ConfigItem, Class<?>> itemsHolder = LinkedHashMultimap.create();
    private final Map<Class<?>, ItemInfo> detailsHolder = Maps.newHashMap();

    public ConfigurationInfo(final ConfigurationContext context) {
        // convert all objects into types (more suitable for analysis)
        for (ConfigItem type : ConfigItem.values()) {
            for (Object item : context.getItems(type)) {
                final Class<?> itemType = getType(item);
                itemsHolder.put(type, itemType);
                detailsHolder.put(itemType, context.getInfo(item));
            }
        }
    }

    /**
     * Pay attention that disabled (or disabled and never registered) items are also returned.
     *
     * @param type configuration item type
     * @param <T>  expected class
     * @return registered item classes of required type in registration order or empty list if nothing registered
     */
    @SuppressWarnings("unchecked")
    public <T> List<Class<T>> getItems(final ConfigItem type) {
        final Collection res = itemsHolder.get(type);
        return res.isEmpty() ? Collections.<Class<T>>emptyList() : (List<Class<T>>) Lists.newArrayList(res);
    }

    /**
     * Used to query items of one configuration type (e.g. only installers or bundles). Some common filters are
     * predefined in {@link Filters}. Use {@link com.google.common.base.Predicates#and(Iterable)},
     * {@link com.google.common.base.Predicates#or(Iterable)} and other composition methods to reuse default
     * filters.
     * <p>
     * Pay attention that disabled (or disabled and never registered) items are also returned.
     *
     * @param type   configuration item type
     * @param filter predicate to filter definitions
     * @param <T>    expected class
     * @param <K>    expected info container class
     * @return registered item classes in registration order, filtered with provided filter or empty list
     */
    public <T, K extends ItemInfo> List<Class<T>> getItems(final ConfigItem type, final Predicate<K> filter) {
        final List<Class<T>> items = getItems(type);
        return filter(items, filter);
    }

    /**
     * Used to query items of all configuration types. May be useful to build configuration tree (e.g. to search
     * all items configured by bundle or by classpath scan). Some common filters are
     * predefined in {@link Filters}. Use {@link com.google.common.base.Predicates#and(Iterable)},
     * {@link com.google.common.base.Predicates#or(Iterable)} and other composition methods to reuse default
     * filters.
     * <p>
     * Pay attention that disabled (or disabled and never registered) items are also returned.
     *
     * @param filter predicate to filter definitions
     * @return registered item classes in registration order, filtered with provided filter or empty list
     */
    @SuppressWarnings("unchecked")
    public List<Class<Object>> getItems(final Predicate<ItemInfo> filter) {
        final List<Class<Object>> items = (List) Lists.newArrayList(itemsHolder.values());
        return filter(items, filter);
    }

    /**
     * @param item configured item type
     * @param <T>  expected configuration container type
     * @return item configuration info or null if item not registered
     */
    @SuppressWarnings("unchecked")
    public <T extends ItemInfo> T getInfo(final Class<?> item) {
        return (T) detailsHolder.get(item);
    }

    private Class<?> getType(final Object item) {
        return item instanceof Class ? (Class) item : item.getClass();
    }

    private <T, K extends ItemInfo> List<Class<T>> filter(final List<Class<T>> items, final Predicate<K> filter) {
        return Lists.newArrayList(Iterables.filter(items, new Predicate<Class<T>>() {
            @Override
            @SuppressWarnings("unchecked")
            public boolean apply(final @Nonnull Class<T> input) {
                final K info = getInfo(input);
                return filter.apply(info);
            }
        }));
    }
}
