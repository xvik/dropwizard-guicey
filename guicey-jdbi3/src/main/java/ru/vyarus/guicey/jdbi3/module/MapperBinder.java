package ru.vyarus.guicey.jdbi3.module;

import com.google.inject.Inject;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.RowMapper;

import java.util.Set;

/**
 * Supplements {@link ru.vyarus.guicey.jdbi3.installer.MapperInstaller}: installer recognize and report found
 * mappers and this bean will actually register resolved mappers in dbi instance.
 * <p>
 * Delayed initialization used to simplify access to DBI instance (in installer it was hard to do).
 *
 * @author Vyacheslav Rusakov
 * @since 31.08.2018
 */
public class MapperBinder {

    @Inject
    public MapperBinder(final Jdbi dbi, final Set<RowMapper> mappers) {
        mappers.forEach(dbi::registerRowMapper);
    }
}
