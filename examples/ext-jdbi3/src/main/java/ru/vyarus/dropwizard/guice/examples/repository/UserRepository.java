package ru.vyarus.dropwizard.guice.examples.repository;

import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import ru.vyarus.dropwizard.guice.examples.model.User;
import ru.vyarus.dropwizard.guice.examples.repository.mapper.bind.UserBind;
import ru.vyarus.dropwizard.guice.examples.service.RandomNameGenerator;
import ru.vyarus.guicey.jdbi3.installer.repository.JdbiRepository;
import ru.vyarus.guicey.jdbi3.tx.InTransaction;

import javax.inject.Inject;
import java.util.List;

/**
 * Recognized and installed by special installer, registered by jdbi bundle.
 * Repository will be in singleton scope automatically.
 * <p>
 * {@link ru.vyarus.guicey.jdbi3.tx.InTransaction} declares lowest transaction scope to be able to use repository
 * without any extra tx definition (scope may be enlarged).
 *
 * @author Vyacheslav Rusakov
 * @since 01.11.2018
 */
@JdbiRepository
@InTransaction
public interface UserRepository extends Crud<User> {

    @Inject
    RandomNameGenerator getGenerator();

    // sample of hybrid method in repository, using injected service
    default User createRandomUser() {
        final User user = new User();
        user.setName(getGenerator().generateName());
        save(user);
        return user;
    }

    @Override
    @SqlUpdate("insert into users (name, version) values (:name, :version)")
    @GetGeneratedKeys
    long insert(@UserBind User entry);

    @SqlUpdate("update users set version=:version, name=:name where id=:id and version=:version - 1")
    @Override
    int update(@UserBind User entry);

    @SqlQuery("select * from users")
    List<User> findAll();

    @SqlQuery("select * from users where name = :name")
    User findByName(@Bind("name") String name);
}
