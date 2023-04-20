package ru.vyarus.dropwizard.guice.examples;

import io.dropwizard.Application;
import io.dropwizard.db.PooledDataSourceFactory;
import io.dropwizard.flyway.FlywayBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.jdbi.v3.core.h2.H2DatabasePlugin;
import ru.vyarus.dropwizard.guice.GuiceBundle;
import ru.vyarus.guicey.jdbi3.JdbiBundle;

/**
 * @author Vyacheslav Rusakov
 * @since 01.11.2018
 */
public class Jdbi3Application extends Application<Jdbi3AppConfiguration> {

    public static void main(String[] args) throws Exception {
        new Jdbi3Application().run(args);
    }

    @Override
    public void initialize(Bootstrap<Jdbi3AppConfiguration> bootstrap) {

        bootstrap.addBundle(GuiceBundle.builder()
                .enableAutoConfig()
                .bundles(JdbiBundle
                        .<Jdbi3AppConfiguration>forDatabase((conf, env) -> conf.getDatabase())
                        .withPlugins(new H2DatabasePlugin()))
                .build());
        // used for manual run to init db
        bootstrap.addBundle(new FlywayBundle<Jdbi3AppConfiguration>() {
            @Override
            public PooledDataSourceFactory getDataSourceFactory(Jdbi3AppConfiguration configuration) {
                return configuration.getDatabase();
            }
        });
    }

    @Override
    public void run(Jdbi3AppConfiguration configuration, Environment environment) throws Exception {

    }
}
