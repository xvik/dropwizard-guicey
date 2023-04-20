package ru.vyarus.dropwizard.guice.examples;

import io.dropwizard.Application;
import io.dropwizard.db.PooledDataSourceFactory;
import io.dropwizard.flyway.FlywayBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import ru.vyarus.dropwizard.guice.GuiceBundle;
import ru.vyarus.guicey.jdbi.JdbiBundle;

/**
 * @author Vyacheslav Rusakov
 * @since 09.12.2016
 */
public class JdbiApplication extends Application<JdbiAppConfiguration> {

    public static void main(String[] args) throws Exception {
        new JdbiApplication().run(args);
    }

    @Override
    public void initialize(Bootstrap<JdbiAppConfiguration> bootstrap) {

        bootstrap.addBundle(GuiceBundle.builder()
                .enableAutoConfig()
                .bundles(JdbiBundle.<JdbiAppConfiguration>forDatabase((conf, env) -> conf.getDatabase()))
                .build());
        // used for manual run to init db
        bootstrap.addBundle(new FlywayBundle<JdbiAppConfiguration>() {
            @Override
            public PooledDataSourceFactory getDataSourceFactory(JdbiAppConfiguration configuration) {
                return configuration.getDatabase();
            }
        });
    }

    @Override
    public void run(JdbiAppConfiguration configuration, Environment environment) throws Exception {

    }
}
