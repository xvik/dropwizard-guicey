package ru.vyarus.dropwizard.guice.module.lifecycle;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Base class for event listeners that must be used only once (for example, if multiple listeners
 * would be registered only one must be used. For example, this is useful for diagnostic listeners:
 * {@code .printDiagnosticInfo()} may be used multiple times, but still only one report will appear in console.
 *
 * @author Vyacheslav Rusakov
 * @since 24.10.2019
 */
@SuppressFBWarnings("EQ_COMPARING_CLASS_NAMES")
public abstract class UniqueGuiceyLifecycleListener extends GuiceyLifecycleAdapter {

    @Override
    public boolean equals(final Object obj) {
        // only one bundle instance allowed
        // intentionally check by class name to also detect instances from different class loaders
        return obj != null && getClass().getName().equals(obj.getClass().getName());
    }

    @Override
    public int hashCode() {
        // for data structures relying on hash first, all equal instances must have the same hash to avoid side effects
        return getClass().getName().hashCode();
    }
}
