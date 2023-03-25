package ru.vyarus.guicey.jdbi3.tx;

import org.jdbi.v3.core.transaction.TransactionIsolationLevel;

/**
 * Transaction configuration. If transaction is already started then configuration is just checked for compatibility
 * with current transaction (e.g. same isolation required or non read only transaction under readonly one).
 *
 * @author Vyacheslav Rusakov
 * @since 17.09.2018
 */
@SuppressWarnings("PMD.AvoidFieldNameMatchingMethodName")
public final class TxConfig {

    private TransactionIsolationLevel level = TransactionIsolationLevel.UNKNOWN;
    private boolean readOnly;

    /**
     * @return configured isolation level
     */
    public TransactionIsolationLevel getLevel() {
        return level;
    }

    /**
     * @return true for read only transaction
     */
    public boolean isReadOnly() {
        return readOnly;
    }

    /**
     * @return true when non default level set
     */
    public boolean isLevelSet() {
        return level != TransactionIsolationLevel.UNKNOWN;
    }

    /**
     * @param level transaction isolation level
     * @return config itself for chained calls
     */
    public TxConfig level(final TransactionIsolationLevel level) {
        this.level = level;
        return this;
    }

    /**
     * @param readOnly true for read only transaction
     * @return config itself for chained calls
     */
    public TxConfig readOnly(final boolean readOnly) {
        this.readOnly = readOnly;
        return this;
    }
}
