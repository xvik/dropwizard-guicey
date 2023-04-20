package ru.vyarus.guicey.jdbi.support.repository

import org.skife.jdbi.v2.sqlobject.SqlQuery
import org.skife.jdbi.v2.sqlobject.SqlUpdate
import ru.vyarus.guicey.jdbi.installer.repository.JdbiRepository
import ru.vyarus.guicey.jdbi.support.mapper.binder.SampleBind
import ru.vyarus.guicey.jdbi.support.model.Sample
import ru.vyarus.guicey.jdbi.tx.InTransaction

/**
 * @author Vyacheslav Rusakov
 * @since 05.12.2016
 */
@JdbiRepository
@InTransaction
interface SampleRepository {

    @SqlQuery("select * from sample")
    List<Sample> all()

    @SqlUpdate("insert into sample (name) values (:name)")
    void save(@SampleBind Sample sample)
}
