package ru.vyarus.dropwizard.guice.examples.repository.mapper;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;
import ru.vyarus.dropwizard.guice.examples.model.User;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Installed with special installer, registered by JDBI bundle.
 *
 * @author Vyacheslav Rusakov
 * @since 09.12.2016
 */
public class UserMapper implements ResultSetMapper<User> {

    @Override
    public User map(int index, ResultSet r, StatementContext ctx) throws SQLException {
        User user = new User();
        user.setId(r.getLong("id"));
        user.setVersion(r.getInt("version"));
        user.setName(r.getString("name"));
        return user;
    }
}
