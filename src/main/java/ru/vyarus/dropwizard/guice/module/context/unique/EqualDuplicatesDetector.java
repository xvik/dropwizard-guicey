package ru.vyarus.dropwizard.guice.module.context.unique;

import ru.vyarus.dropwizard.guice.module.context.info.ItemInfo;

import java.util.Collection;

/**
 * Default configuration duplicates detector. Only equal instances are considered duplicate, so in order to grant
 * bundle or module uniqueness simply implement correct equals method.
 * <p>
 * By default, only the same instances, registered multiple times will be considered duplicate (due to default equals
 * implementation).
 *
 * @author Vyacheslav Rusakov
 * @since 03.07.2019
 */
public class EqualDuplicatesDetector implements DuplicateConfigDetector {

    @Override
    public boolean isDuplicate(final ItemInfo info,
                               final Collection<Object> registered,
                               final Object newItem) {
        // instance uniqueness is based on equals
        for (Object reg : registered) {
            if (reg.equals(newItem)) {
                return true;
            }
        }
        return false;
    }
}
