package ru.vyarus.guicey.jdbi.tx;

import org.skife.jdbi.v2.Handle;

/**
 * Transaction action passed to transaction template.
 *
 * @param <T> return type
 * @author Vyacheslav Rusakov
 * @see TransactionTemplate for usage
 * @since 4.12.2016
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
