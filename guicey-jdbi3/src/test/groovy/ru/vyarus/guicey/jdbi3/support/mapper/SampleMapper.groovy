package ru.vyarus.guicey.jdbi3.support.mapper

import org.jdbi.v3.core.mapper.RowMapper
import org.jdbi.v3.core.statement.StatementContext
import ru.vyarus.guicey.jdbi3.support.model.Sample

import java.sql.ResultSet
import java.sql.SQLException

/**
 * @author Vyacheslav Rusakov
 * @since 31.08.2018
 */
class SampleMapper implements RowMapper<Sample> {

    @Override
    Sample map(ResultSet rs, StatementContext ctx) throws SQLException {
        return new Sample(id: rs.getLong("id"),
                name: rs.getString("name"))
    }
}
