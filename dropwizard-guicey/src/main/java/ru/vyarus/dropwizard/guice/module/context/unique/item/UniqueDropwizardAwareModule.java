package ru.vyarus.dropwizard.guice.module.context.unique.item;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.dropwizard.core.Configuration;
import ru.vyarus.dropwizard.guice.module.support.DropwizardAwareModule;

/**
 * Base class for unique guice modules with access to dropwizard specific objects. Should be used
 * when {@link DropwizardAwareModule} should be unique, but it is impossible to also extend {@link UniqueModule}.
 * Read their javadocs for more details as current class combines abilities of both.
 *
 * @param <C> configuration type
 * @author Vyacheslav Rusakov
 * @since 13.12.2019
 */
@SuppressFBWarnings("EQ_COMPARING_CLASS_NAMES")
public abstract class UniqueDropwizardAwareModule<C extends Configuration> extends DropwizardAwareModule<C> {

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
