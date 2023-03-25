package ru.vyarus.guicey.jdbi3.dbi;

import io.dropwizard.core.Configuration;
import io.dropwizard.db.PooledDataSourceFactory;
import io.dropwizard.jdbi3.JdbiFactory;
import io.dropwizard.core.setup.Environment;
import org.jdbi.v3.core.Jdbi;

/**
 * Simple DBI configurer, requiring just database configuration.
 *
 * @param <C> configuration type
 * @author Vyacheslav Rusakov
 * @since 31.08.2018
 */
public class SimpleDbiProvider<C extends Configuration> implements ConfigAwareProvider<Jdbi, C> {

    private final ConfigAwareProvider<PooledDataSourceFactory, C> database;

    public SimpleDbiProvider(final ConfigAwareProvider<PooledDataSourceFactory, C> database) {
        this.database = database;
    }

    @Override
    public Jdbi get(final C configuration, final Environment environment) {
        return new JdbiFactory().build(environment, database.get(configuration, environment), "db");
    }
}
