package ru.vyarus.guicey.jdbi3.support.repository.syntetic;

import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import ru.vyarus.guicey.jdbi3.installer.repository.JdbiRepository;
import ru.vyarus.guicey.jdbi3.tx.InTransaction;

/**
 * @author Vyacheslav Rusakov
 * @since 23.11.2022
 */
@JdbiRepository
@InTransaction
public interface RootRepo extends BaseRepo<NamedEntity> {

    @Override
    @SqlUpdate("insert into sample (name) values (:name)")
    @GetGeneratedKeys
    long save(@NamedBind NamedEntity sample);

    @Override
    @SqlQuery("select * from sample where id=:id")
    NamedEntity get(@Bind("id") long id);
}
