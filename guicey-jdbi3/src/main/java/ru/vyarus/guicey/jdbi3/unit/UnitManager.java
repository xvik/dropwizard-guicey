package ru.vyarus.guicey.jdbi3.unit;

import com.google.common.base.Preconditions;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vyarus.guicey.jdbi3.tx.TransactionTemplate;

import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.inject.Singleton;

/**
 * Manages JDBI {@link Handle} for current unit of work. This handle must be used by all JDBI proxies.
 * Unit of work is thread-bound (all actions in one thread participate in one unit of work).
 * It is not intended to be used directly (only in really rare cases when manual unit required
 * without opening transaction).
 * <p>
 * Raw provider may be injected to obtain current handle: {@code @Inject Provider<Handle>}.
 * In all other cases transaction annotation must be used to wrap code into unit of work using guice aop.
 *
 * @author Vyacheslav Rusakov
 * @see TransactionTemplate for manual transaction definition
 * @since 31.08.2018
 */
@Singleton
public class UnitManager implements Provider<Handle> {

    private final Logger logger = LoggerFactory.getLogger(UnitManager.class);

    private final Jdbi jdbi;
    private final ThreadLocal<Handle> unit = new ThreadLocal<>();

    /**
     * Create a unit manager.
     *
     * @param jdbi jdbi instance
     */
    @Inject
    public UnitManager(final Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    @Override
    public Handle get() {
        Preconditions.checkState(isUnitStarted(), "Unit of work not started yet");
        return unit.get();
    }

    /**
     * @return true if unit of work started (and handle could be obtained), false otherwise
     */
    public boolean isUnitStarted() {
        return unit.get() != null;
    }

    /**
     * Starts unit of work.
     *
     * @throws IllegalStateException if unit of work already started
     */
    public void beginUnit() {
        Preconditions.checkState(!isUnitStarted(), "Unit of work already started");
        final Handle handle = jdbi.open();
        unit.set(handle);
        logger.trace("Transaction start");
    }

    /**
     * Finish unit of work. Note: does not commit transaction, but only close context handle.
     *
     * @throws IllegalStateException when no opened unit of work
     */
    public void endUnit() {
        Preconditions.checkState(isUnitStarted(), "Stop called outside of unit of work");
        final Handle handle = unit.get();
        // first remove handle to avoid stale handles in any case
        unit.remove();
        try {
            handle.close();
        } catch (Exception ex) {
            // not entire stacktrace to avoid confusion: it may appear here only because of connection damage
            // and so there will already be logged traces indicating connection problem
            logger.warn("JDBI handle close error ({})", ex.getMessage());
        }
        logger.trace("Transaction end");
    }
}
