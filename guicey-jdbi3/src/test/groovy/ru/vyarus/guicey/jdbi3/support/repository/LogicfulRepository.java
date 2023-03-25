package ru.vyarus.guicey.jdbi3.support.repository;

import com.google.common.base.Preconditions;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import ru.vyarus.guicey.jdbi3.installer.repository.JdbiRepository;
import ru.vyarus.guicey.jdbi3.support.model.Sample;
import ru.vyarus.guicey.jdbi3.tx.InTransaction;

import javax.inject.Inject;
import java.util.List;

/**
 * @author Vyacheslav Rusakov
 * @since 17.09.2018
 */
@JdbiRepository
@InTransaction
public interface LogicfulRepository {

    @SqlQuery("select * from sample")
    List<Sample> all();

    @Inject
    CustTxRepository getCustRepo();

    default List<Sample> checkInject() {
        List<Sample> all = all();
        List<Sample> all2 = getCustRepo().all();
        Preconditions.checkState(all.size() == all2.size());
        return all2;
    }
}
