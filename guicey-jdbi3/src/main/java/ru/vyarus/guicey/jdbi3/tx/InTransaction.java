package ru.vyarus.guicey.jdbi3.tx;

import org.jdbi.v3.core.transaction.TransactionIsolationLevel;
import ru.vyarus.guicey.jdbi3.tx.aop.config.InTransactionTxConfigFactory;
import ru.vyarus.guicey.jdbi3.tx.aop.config.TxConfigSupport;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for unit of work and transaction declaration. Code executed under the scope of annotation will
 * share the same transaction (and handle).
 * <p>
 * Use on class to mark all methods as transactional.
 * <p>
 * Support nesting: nested annotated elements will participate in outer transaction (and so exceptions will rollback
 * entire transaction). If nested transaction configuration contradict with ongoing transaction then exception
 * will be thrown (e.g. different isolation level or write required under read only transaction).
 * <p>
 * NOTE: jdbi transaction annotation ({@link org.jdbi.v3.sqlobject.transaction.Transaction}) is not used to avoid
 * internal jdbi transaction handling mechanism which may contradict with guice-central transactional mechanism
 * (because simply jdbi is not aware of it and did not expect anyone to manage transaction instead).
 *
 * @author Vyacheslav Rusakov
 * @since 31.08.2018
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@TxConfigSupport(InTransactionTxConfigFactory.class)
public @interface InTransaction {

    /**
     * @return the transaction isolation level.  If not specified, invoke with the default isolation level.
     */
    TransactionIsolationLevel value() default TransactionIsolationLevel.UNKNOWN;
    /**
     * Set the connection readOnly property before the transaction starts, and restore it before it returns.
     * Databases may use this as a performance or concurrency hint.
     * @return whether the transaction is read only
     */
    boolean readOnly() default false;
}
