package ru.vyarus.guicey.jdbi.module;

import com.google.inject.Inject;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import java.util.Set;

/**
 * Supplements {@link ru.vyarus.guicey.jdbi.installer.MapperInstaller}: installer recognize and report found
 * mappers and this bean will actually register resolved mappers in dbi instance.
 * <p>
 * Delayed initialization used to simplify access to DBI instance (in installer it was hard to do).
 *
 * @author Vyacheslav Rusakov
 * @since 05.12.2016
 */
public class MapperBinder {

    @Inject
    public MapperBinder(final DBI dbi, final Set<ResultSetMapper> mappers) {
        mappers.forEach(dbi::registerMapper);
    }
}
