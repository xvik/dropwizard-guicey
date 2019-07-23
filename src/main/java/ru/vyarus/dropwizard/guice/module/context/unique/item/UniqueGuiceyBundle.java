package ru.vyarus.dropwizard.guice.module.context.unique.item;

import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBundle;

/**
 * Base class for unique bundles: only one bundle instance must be accepted and all others considered duplicate.
 * Note that class only properly implements equals method so guicey deduplication mechanism could filter other
 * instances. It is not required to use this class to grant bundle uniqueness - you may directly implement equals
 * method in your bundle.
 *
 * @author Vyacheslav Rusakov
 * @see ru.vyarus.dropwizard.guice.GuiceBundle.Builder#duplicateConfigDetector(
 *ru.vyarus.dropwizard.guice.module.context.unique.DuplicateConfigDetector)
 * @since 13.07.2019
 */
public abstract class UniqueGuiceyBundle implements GuiceyBundle {

    @Override
    public boolean equals(final Object obj) {
        // only one bundle instance allowed
        return obj != null && getClass().equals(obj.getClass());
    }

    @Override
    public int hashCode() {
        // for data structures relying on hash first, all equal instances must have the same hash to avoid side effects
        return getClass().hashCode();
    }
}
