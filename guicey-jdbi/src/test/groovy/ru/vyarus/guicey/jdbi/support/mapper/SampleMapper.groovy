package ru.vyarus.guicey.jdbi.support.mapper

import org.skife.jdbi.v2.StatementContext
import org.skife.jdbi.v2.tweak.ResultSetMapper
import ru.vyarus.guicey.jdbi.support.model.Sample

import java.sql.ResultSet
import java.sql.SQLException

/**
 * @author Vyacheslav Rusakov
 * @since 05.12.2016
 */
class SampleMapper implements ResultSetMapper<Sample> {

    @Override
    Sample map(int index, ResultSet r, StatementContext ctx) throws SQLException {
        return new Sample(id: r.getLong("id"),
                name: r.getString("name"))
    }
}
