package ru.vyarus.dropwizard.guice.module.context.unique.item;

import com.google.inject.AbstractModule;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Base class for unique modules: only one module instance must be accepted and all others considered duplicate.
 * Note that class only properly implements equals method so guicey deduplication mechanism could filter other
 * instances. It is not required to use this class to grant module uniqueness - you may directly implement equals
 * method in your module.
 * <p>
 * Classed are compared by name to properly detect classes from different class loaders.
 * <p>
 * Also, note that guice silently ignores duplicate bindings and so in most cases it would be able to
 * handle duplicate modules properly. This class may be used for cases when bindings registered by module
 * instances are not equal (e.g. register something by instance or produce other side effects).
 * <p>
 * If module requires access to dropwizard specific project (via
 * {@link ru.vyarus.dropwizard.guice.module.support.DropwizardAwareModule}) then use
 * {@link UniqueDropwizardAwareModule} instead.
 *
 * @author Vyacheslav Rusakov
 * @see ru.vyarus.dropwizard.guice.GuiceBundle.Builder#duplicateConfigDetector(
 *ru.vyarus.dropwizard.guice.module.context.unique.DuplicateConfigDetector)
 * @since 13.07.2019
 */
@SuppressFBWarnings("EQ_COMPARING_CLASS_NAMES")
public abstract class UniqueModule extends AbstractModule {

    @Override
    public boolean equals(final Object obj) {
        // only one module instance allowed
        // intentionally check by class name to also detect instances from different class loaders
        return obj != null && getClass().getName().equals(obj.getClass().getName());
    }

    @Override
    public int hashCode() {
        // for data structures relying on hash first, all equal instances must have the same hash to avoid side effects
        return getClass().getName().hashCode();
    }
}
