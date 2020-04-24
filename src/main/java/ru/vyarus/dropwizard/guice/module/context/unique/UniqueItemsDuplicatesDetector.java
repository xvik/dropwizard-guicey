package ru.vyarus.dropwizard.guice.module.context.unique;

import com.google.common.base.Preconditions;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Simple deduplicator implementation to track uniqueness for exact items (not everything like
 * {@link LegacyModeDuplicatesDetector} do).
 *
 * @author Vyacheslav Rusakov
 * @since 24.09.2019
 */
public class UniqueItemsDuplicatesDetector implements DuplicateConfigDetector {

    private final Set<String> items = new HashSet<>();

    public UniqueItemsDuplicatesDetector(final Class<?>... uniqueItems) {
        Preconditions.checkArgument(uniqueItems.length > 0, "No unique items to configured");
        // use strings to correctly detect class from different class loaders (as core mechanism will correctly
        // detect them)
        for (Class<?> cls : uniqueItems) {
            items.add(id(cls));
        }
    }

    @Override
    public Object getDuplicateItem(final List<Object> registered, final Object newItem) {
        return items.contains(id(newItem.getClass())) ? registered.get(0) : null;
    }

    private String id(final Class<?> cls) {
        return cls.getName();
    }
}
