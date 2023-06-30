package ru.vyarus.guicey.jdbi3.tx;


import com.google.common.base.Throwables;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.HandleCallback;
import org.jdbi.v3.core.transaction.TransactionException;
import org.jdbi.v3.core.transaction.TransactionIsolationLevel;
import ru.vyarus.guicey.jdbi3.unit.UnitManager;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Transaction template used to both declare unit of work and start transaction.
 * If called inside of transaction then provided action will be simply executed as transaction is already managed
 * somewhere outside. In case of exception, it's propagated and transaction rolled back.
 * <p>
 * Usage:
 * <pre><code>
 *    {@literal @}Inject TransactionTemplate template;
 *     ...
 *     template.inTransaction(() -&gt; doSoemStaff())
 * </code></pre>
 *
 * @author Vyacheslav Rusakov
 * @since 31.08.2018
 */
@Singleton
public class TransactionTemplate {

    private final UnitManager manager;

    @Inject
    public TransactionTemplate(final UnitManager manager) {
        this.manager = manager;
    }

    /**
     * Shortcut for {@link #inTransaction(TxConfig, TxAction)} for calling action with default transaction config.
     *
     * @param action action to execute
     * @param <T>    return type
     * @return action result
     */
    @SuppressWarnings("PMD.AvoidThrowingRawExceptionTypes")
    public <T> T inTransaction(final TxAction<T> action) {
        return inTransaction(new TxConfig(), action);
    }

    /**
     * Wraps provided action with unit of work and transaction. If called under already started transaction
     * then action will be called directly.
     * <p>
     * NOTE: If unit of work was started manually (using {@link UnitManager}, but without transaction started,
     * then action will be simply executed without starting transaction. This was done for rare situations
     * when logic must be performed without transaction and transaction annotation will simply indicate unit of work.
     *
     * @param config transaction config
     * @param action action to execute
     * @param <T>    return type
     * @return action result
     */
    @SuppressWarnings("PMD.AvoidThrowingRawExceptionTypes")
    public <T> T inTransaction(final TxConfig config, final TxAction<T> action) {
        if (manager.isUnitStarted()) {
            // already started
            try {
                return inCurrentTransaction(config, action);
            } catch (Throwable th) {
                Throwables.throwIfUnchecked(th);
                throw new RuntimeException(th);
            }
        } else {
            manager.beginUnit();
            try {
                return inNewTransaction(config, action);
            } finally {
                manager.endUnit();
            }
        }
    }

    private <T> T inCurrentTransaction(final TxConfig config, final TxAction<T> action) throws Exception {
        // mostly copies org.jdbi.v3.sqlobject.transaction.internal.TransactionDecorator logic
        final Handle h = manager.get();
        if (config.isLevelSet()) {
            final TransactionIsolationLevel currentLevel = h.getTransactionIsolationLevel();
            if (currentLevel != config.getLevel()) {
                throw new TransactionException("Tried to execute nested @Transaction(" + config.getLevel() + "), "
                        + "but already running in a transaction with isolation level " + currentLevel + ".");
            }
        }
        if (h.isReadOnly() && !config.isReadOnly()) {
            throw new TransactionException("Tried to execute a nested @Transaction(readOnly=false) "
                    + "inside a readOnly transaction");
        }
        return action.execute(h);
    }

    @SuppressWarnings("PMD.AvoidThrowingRawExceptionTypes")
    private <T> T inNewTransaction(final TxConfig config, final TxAction<T> action) {
        final Handle h = manager.get();
        h.setReadOnly(config.isReadOnly());
        final HandleCallback<T, RuntimeException> callback = handle -> {
            try {
                return action.execute(handle);
            } catch (Exception e) {
                Throwables.throwIfUnchecked(e);
                throw new RuntimeException(e);
            }
        };
        return config.isLevelSet() ? h.inTransaction(config.getLevel(), callback) : h.inTransaction(callback);
    }
}
