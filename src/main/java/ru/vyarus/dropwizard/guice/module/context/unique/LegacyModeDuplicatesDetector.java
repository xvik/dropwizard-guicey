package ru.vyarus.dropwizard.guice.module.context.unique;

import java.util.List;

/**
 * Duplicates detector, implementing legacy guicey behaviour: only one instance of class is allowed.
 * It could be used during migrations from previous guicey versions (when new behaviour is not profitable because
 * too much of current logic assumes duplicates eviction).
 * <p>
 * See also {@link ru.vyarus.dropwizard.guice.GuiceBundle.Builder#uniqueItems(Class[])} if only few types
 * actually require deduplication (less restrictive option).
 *
 * @author Vyacheslav Rusakov
 * @since 03.07.2019
 */
public class LegacyModeDuplicatesDetector implements DuplicateConfigDetector {

    @Override
    public Object getDuplicateItem(final List<Object> registered,
                                   final Object newItem) {
        // method will be called only if instance of the same type is already registered
        return registered.get(0);
    }
}
