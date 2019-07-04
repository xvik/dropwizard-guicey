package ru.vyarus.dropwizard.guice.module.context.unique;

import ru.vyarus.dropwizard.guice.module.context.info.ItemInfo;

import java.util.Collection;

/**
 * Duplicates detector, implementing legacy guicey behaviour: only one instance of class is allowed.
 * It could be used during migrations from previous guicey versions (when new behaviour is not profitable because
 * too much of current logic assumes duplicates eviction).
 *
 * @author Vyacheslav Rusakov
 * @since 03.07.2019
 */
public class LegacyModeDuplicatesDetector implements DuplicateConfigDetector {

    @Override
    public boolean isDuplicate(final ItemInfo info,
                               final Collection<Object> registered,
                               final Object newItem) {
        return true;
    }
}
