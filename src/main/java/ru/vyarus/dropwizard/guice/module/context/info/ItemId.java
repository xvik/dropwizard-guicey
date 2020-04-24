package ru.vyarus.dropwizard.guice.module.context.info;

import com.google.common.base.Preconditions;

import java.util.*;

/**
 * As multiple instances of the same type may be used during configuration (e.g. bundle or module) then it's not
 * enough to declare just class in order to identify type.
 * <p>
 * Identity string is generated using object hash code (the same way as used in default {@link Object#toString()}.
 * It is assumed that no one would override bundle hash code (who would be?) and toString so using the same
 * hash as in default toString would be ideal for instance identification (at least instance could be easily
 * associated in debugger).
 * <p>
 * Type has special equals behaviour: id, created from class only is equal to any instance identity (containing
 * instance identifier). This simplifies searches by class only. In order to make this work, hash code for itemId
 * is the same for all ids of the same type.
 * <p>
 * Reminder:
 * <ul>
 * <li>Identity object could always be prepared from instance: {@code ItemId.from(instance)}</li>
 * <li>Class item could be used to detect instanes: e.g. {@code listOfIds.contains(ItemId.from(MyModule.class))}
 * will be true if instance id is contained in collection.</li>
 * </ul>
 *
 * @param <T> class type
 * @author Vyacheslav Rusakov
 * @since 05.07.2019
 */
public final class ItemId<T> {

    private final Class<T> type;
    private final String id;

    @SuppressWarnings("unchecked")
    private ItemId(final Class<?> type, final String identity) {
        this.type = (Class<T>) Preconditions.checkNotNull(type);
        this.id = identity;
    }

    /**
     * @return type
     */
    public Class<T> getType() {
        return type;
    }

    /**
     * @return object instance hash or null
     */
    public String getIdentity() {
        return id;
    }

    @Override
    public String toString() {
        return type.getSimpleName() + (id == null ? "" : "@" + id);
    }

    /**
     * Creates object instance identity string. Returned code is the same as number part
     * after "@" in default {@link Object#toString()}.
     *
     * @param instance object instance
     * @return identity string (hash)
     */
    public static String identity(final Object instance) {
        return Integer.toHexString(System.identityHashCode(instance));
    }

    /**
     * Construct type key for instance of class. When called with class instance it's equivalent to
     * {@link #from(Class)} call.
     *
     * @param instance instance of object representing type (bundle, modules) or class
     * @param <T>      class type
     * @return type key
     */
    public static <T> ItemId<T> from(final Object instance) {
        return instance instanceof Class
                ? from((Class) instance)
                : new ItemId<T>(instance.getClass(), identity(instance));
    }

    /**
     * Construct type key for class only. Useful when only one instance of type class is used.
     *
     * @param type type class
     * @param <T>  class type
     * @return type key
     */
    public static <T> ItemId<T> from(final Class<?> type) {
        return new ItemId<T>(type, null);
    }

    /**
     * Utility method to project collections of item ids into classes. If list contains multiple instance ids of
     * the same type then resulted list will contain only one type (and so resulted list will be shorter).
     *
     * @param items item ids
     * @param <T>   target items class type (when possible to declare)
     * @return list of classes from provided ids
     */
    public static <T> List<Class<T>> typesOnly(final Collection<ItemId<T>> items) {
        if (items.isEmpty()) {
            return Collections.emptyList();
        }
        final List<Class<T>> res = new ArrayList<>();
        for (ItemId<T> item : items) {
            // all instance ids are merged into one type (positioned on first occurrence)
            if (!res.contains(item.getType())) {
                res.add(item.getType());
            }
        }
        return res;
    }

    @Override
    @SuppressWarnings({"checkstyle:NeedBraces", "PMD.ControlStatementBraces"})
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof ItemId)) return false;

        final ItemId key = (ItemId) o;

        if (!type.equals(key.type)) return false;
        // identity ignore is allowed for type only searches so pure class id is equal to any instance id
        return Objects.equals(id, key.id) || id == null || key.getIdentity() == null;
    }

    @Override
    public int hashCode() {
        // IMPORTANT: items of the same class will produce THE SAME hash codes
        // This is important for proper equals work in cases when instance items must be matched by class item
        // Without this behaviour would be inconsistent between different structures
        // (for example, for ArrayList(with instance ids).contains(class id) will correctly match and
        // LinkedHashMap will not match at all (because it relies on hash first;
        // The same problem with HashMaps)
        return type.hashCode();
    }
}
