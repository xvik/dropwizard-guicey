package ru.vyarus.guicey.jdbi3

import com.google.inject.Provider
import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.transaction.TransactionIsolationLevel
import ru.vyarus.guicey.jdbi3.tx.InTransaction

import jakarta.inject.Inject
import jakarta.inject.Singleton

/**
 * @author Vyacheslav Rusakov
 * @since 28.09.2018
 */
class TxConfigurationTest extends AbstractAppTest {

    @Inject
    TxService service

    def "Check tx configuration appliance"() {

        expect:
        service.defLevelCall() == TransactionIsolationLevel.READ_COMMITTED
        service.custLevelCall() == TransactionIsolationLevel.READ_UNCOMMITTED
        !service.defReadOnly()
        !service.readOnly() // h2 ignores this flag
    }

    @Singleton
    static class TxService {

        @Inject
        Provider<Handle> handle

        @InTransaction()
        TransactionIsolationLevel defLevelCall() {
            return handle.get().getTransactionIsolationLevel()
        }

        @InTransaction(TransactionIsolationLevel.READ_UNCOMMITTED)
        TransactionIsolationLevel custLevelCall() {
            return handle.get().getTransactionIsolationLevel()
        }

        @InTransaction()
        boolean defReadOnly() {
            return handle.get().isReadOnly()
        }

        @InTransaction(readOnly = true)
        boolean readOnly() {
            return handle.get().isReadOnly()
        }
    }
}
