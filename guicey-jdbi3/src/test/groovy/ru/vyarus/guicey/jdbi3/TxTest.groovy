package ru.vyarus.guicey.jdbi3

import com.google.inject.Provider
import com.google.inject.ProvisionException
import org.jdbi.v3.core.Handle
import ru.vyarus.guicey.jdbi3.support.model.Sample
import ru.vyarus.guicey.jdbi3.support.repository.CustTxRepository
import ru.vyarus.guicey.jdbi3.support.repository.SampleRepository
import ru.vyarus.guicey.jdbi3.tx.TransactionTemplate

import javax.inject.Inject

/**
 * @author Vyacheslav Rusakov
 * @since 31.08.2018
 */
class TxTest extends AbstractAppTest {

    @Inject
    CustTxRepository notxrepo
    @Inject
    SampleRepository repo
    @Inject
    Provider<Handle> handle
    @Inject
    TransactionTemplate template

    def "Check handle access"() {

        when: 'get handle outside of tx'
        handle.get()
        then: "err"
        thrown(ProvisionException)

        when: 'accessing inside tx'
        template.inTransaction { handle.get() }
        then: "ok"
        true
    }

    def "Check dao access"() {

        when: "accessing dao without tx"
        notxrepo.all()
        then: 'err'
        thrown(ProvisionException)

        when: "accessing dao in tx"
        template.inTransaction { notxrepo.all() }
        then: "ok"
        true

        when: "accessing annotated dao"
        repo.all()
        then: "ok"
        true
    }

    def "Check nested tx"() {

        when: "call annotated repo inside tx"
        template.inTransaction({
            notxrepo.save(new Sample(name: 'test'))
            // nested tx
            assert repo.all().size() == 1
        })
        then: "ok"
        true

    }

    def "Check rollback"() {

        when: "fail tx"
        template.inTransaction({
            notxrepo.save(new Sample(name: 'test'))
            throw new IllegalStateException("ups")
        })
        then: "ex propagated and state rolled back"
        thrown(IllegalStateException)
        repo.all().isEmpty()

    }

    def "Check nested tx rollback"() {

        when: 'exception in nested'
        template.inTransaction({
            notxrepo.save(new Sample(name: 'test'))
            template.inTransaction({
                notxrepo.save(new Sample(name: 'test2'))
                assert notxrepo.all().size() == 2
                throw new IllegalStateException("ups")
            })
        })
        then: "ex propagated and state rolled back"
        thrown(IllegalStateException)
        repo.all().isEmpty()
    }
}