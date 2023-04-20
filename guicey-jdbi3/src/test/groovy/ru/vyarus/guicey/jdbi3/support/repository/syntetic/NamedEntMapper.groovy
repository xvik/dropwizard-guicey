package ru.vyarus.guicey.jdbi3.support.repository.syntetic

import org.jdbi.v3.core.mapper.RowMapper
import org.jdbi.v3.core.statement.StatementContext

import java.sql.ResultSet
import java.sql.SQLException

/**
 * @author Vyacheslav Rusakov
 * @since 31.08.2018
 */
class NamedEntMapper implements RowMapper<NamedEntity> {

    @Override
    NamedEntity map(ResultSet rs, StatementContext ctx) throws SQLException {
        return new NamedEntity(id: rs.getLong("id"),
                name: rs.getString("name"))
    }
}
