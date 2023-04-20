package ru.vyarus.guicey.jdbi3

import ru.vyarus.guicey.jdbi3.support.model.Sample
import ru.vyarus.guicey.jdbi3.support.repository.CustTxRepository
import ru.vyarus.guicey.jdbi3.support.repository.LogicfulRepository
import ru.vyarus.guicey.jdbi3.support.repository.SampleRepository

import javax.inject.Inject

/**
 * @author Vyacheslav Rusakov
 * @since 17.09.2018
 */
class InjectionGetterTest extends AbstractAppTest {

    @Inject
    SampleRepository repo

    @Inject
    LogicfulRepository repo2

    def "Check injector getter"() {

        expect: "getter works"
        repo.custRepo instanceof CustTxRepository
    }

    def "Check default method"() {

        when: "filling repo"
        repo.save(new Sample(name: "test1"))
        repo.save(new Sample(name: "test2"))

        then: "access through injection works"
        repo2.checkInject().size() == 2
    }
}
