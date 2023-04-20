package ru.vyarus.guicey.jdbi.dbi;

import io.dropwizard.Configuration;
import io.dropwizard.db.PooledDataSourceFactory;
import io.dropwizard.jdbi.DBIFactory;
import io.dropwizard.setup.Environment;
import org.skife.jdbi.v2.DBI;

/**
 * Simple DBI configurer, requiring just database configuration.
 *
 * @param <C> configuration type
 * @author Vyacheslav Rusakov
 * @since 05.12.2016
 */
public class SimpleDbiProvider<C extends Configuration> implements ConfigAwareProvider<DBI, C> {

    private final ConfigAwareProvider<PooledDataSourceFactory, C> database;

    public SimpleDbiProvider(final ConfigAwareProvider<PooledDataSourceFactory, C> database) {
        this.database = database;
    }

    @Override
    public DBI get(final C configuration, final Environment environment) {
        return new DBIFactory().build(environment, database.get(configuration, environment), "db");
    }
}
