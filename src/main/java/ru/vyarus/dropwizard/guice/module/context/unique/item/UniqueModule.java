package ru.vyarus.dropwizard.guice.module.context.unique.item;

import com.google.inject.AbstractModule;

/**
 * Base class for unique modules: only one module instance must be accepted and all others considered duplicate.
 * Note that class only properly implements equals method so guicey deduplication mechanism could filter other
 * instances. It is not required to use this class to grant module uniqueness - you may directly implement equals
 * method in your module.
 * <p>
 * Also, note that guice silently ignores duplicate bindings and so in most cases it would be able to
 * handle duplicate modules properly. This class may be used for cases when bindings registered by module
 * instances are not equal (e.g. register something by instance or produce other side effects).
 *
 * @author Vyacheslav Rusakov
 * @see ru.vyarus.dropwizard.guice.GuiceBundle.Builder#duplicateConfigDetector(
 *ru.vyarus.dropwizard.guice.module.context.unique.DuplicateConfigDetector)
 * @since 13.07.2019
 */
public abstract class UniqueModule extends AbstractModule {

    @Override
    public boolean equals(final Object obj) {
        // only one debug module instance allowed
        return obj != null && getClass().equals(obj.getClass());
    }

    @Override
    public int hashCode() {
        // for data structures relying on hash first, all equal instances must have the same hash to avoid side effects
        return getClass().hashCode();
    }
}
