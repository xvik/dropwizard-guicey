package ru.vyarus.guicey.jdbi.tx;


import com.google.common.base.Throwables;
import ru.vyarus.guicey.jdbi.unit.UnitManager;

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
 * @since 4.12.2016
 */
@Singleton
public class TransactionTemplate {

    private final UnitManager manager;

    @Inject
    public TransactionTemplate(final UnitManager manager) {
        this.manager = manager;
    }

    /**
     * Wraps provided action with unit of work and transaction. If called under already started transaction
     * then action will be called directly.
     * <p>
     * NOTE: If unit of work was started manually (using {@link UnitManager}, but without transaction started,
     * then action will be simply executed without starting transaction. This was done for rare situations
     * when logic must be performed without transaction and transaction annotation will simply indicate unit of work.
     *
     * @param action action to execute
     * @param <T>    return type
     * @return action result
     */
    @SuppressWarnings("PMD.AvoidThrowingRawExceptionTypes")
    public <T> T inTransaction(final TxAction<T> action) {
        if (manager.isUnitStarted()) {
            // already started
            try {
                return action.execute(manager.get());
            } catch (Throwable th) {
                Throwables.throwIfUnchecked(th);
                throw new RuntimeException(th);
            }
        } else {
            manager.beginUnit();
            try {
                return manager.get()
                        .inTransaction((conn, status) -> action.execute(manager.get()));
            } finally {
                manager.endUnit();
            }
        }
    }
}
