package ru.vyarus.guicey.jdbi3.tx;

import org.jdbi.v3.core.Handle;

/**
 * Transaction action passed to transaction template.
 *
 * @param <T> return type
 * @author Vyacheslav Rusakov
 * @see TransactionTemplate for usage
 * @since 31.08.2018
 */
@FunctionalInterface
public interface TxAction<T> {

    /**
     * Called under transaction. Exceptions are propagated and cause transaction rollback.
     *
     * @param handle current JDBI handle under unit of work
     * @return action result
     * @throws Exception on errors (used to move exceptions handling outside of action)
     */
    T execute(Handle handle) throws Exception;
}
