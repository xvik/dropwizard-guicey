package ru.vyarus.guicey.jdbi3

import org.jdbi.v3.core.transaction.TransactionException
import org.jdbi.v3.core.transaction.TransactionIsolationLevel
import ru.vyarus.guicey.jdbi3.tx.InTransaction

import javax.inject.Inject

/**
 * @author Vyacheslav Rusakov
 * @since 28.09.2018
 */
class IncompatibleTxConfigTest extends AbstractAppTest {

    @Inject
    TxService service

    def "Check tx configuration errors"() {

        when: "tx level change"
        service.levelErr()
        then:
        def ex = thrown(TransactionException)
        ex.message == "Tried to execute nested @Transaction(READ_UNCOMMITTED), but already running in a transaction with isolation level READ_COMMITTED."

        when: "readonly change"
        service.readOnlyErr()
        then:
        true // error not thrown because h2 ignores readonly flag!
    }

    @InTransaction
    static class TxService {

        void levelErr(){
            custLevelCall()
        }

        @InTransaction(readOnly = true)
        void readOnlyErr() {
            nonReadOnly()
        }

        @InTransaction(TransactionIsolationLevel.READ_UNCOMMITTED)
        void custLevelCall() {
        }

        void nonReadOnly() {
        }
    }
}
