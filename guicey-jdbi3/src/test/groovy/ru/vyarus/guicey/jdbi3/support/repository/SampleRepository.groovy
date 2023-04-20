package ru.vyarus.guicey.jdbi3.support.repository

import org.jdbi.v3.sqlobject.statement.SqlQuery
import org.jdbi.v3.sqlobject.statement.SqlUpdate
import ru.vyarus.guicey.jdbi3.installer.repository.JdbiRepository
import ru.vyarus.guicey.jdbi3.support.mapper.binder.SampleBind
import ru.vyarus.guicey.jdbi3.support.model.Sample
import ru.vyarus.guicey.jdbi3.tx.InTransaction

import javax.inject.Inject

/**
 * @author Vyacheslav Rusakov
 * @since 31.08.2018
 */
@JdbiRepository
@InTransaction
interface SampleRepository {

    @SqlQuery("select * from sample")
    List<Sample> all()

    @SqlUpdate("insert into sample (name) values (:name)")
    void save(@SampleBind Sample sample)

    @Inject
    CustTxRepository getCustRepo();
}
