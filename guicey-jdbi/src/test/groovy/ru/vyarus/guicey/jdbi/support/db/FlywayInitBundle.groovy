package ru.vyarus.guicey.jdbi.support.db

import io.dropwizard.db.DataSourceFactory
import io.dropwizard.lifecycle.Managed
import org.flywaydb.core.Flyway
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBootstrap
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBundle
import ru.vyarus.dropwizard.guice.module.installer.order.Order
import ru.vyarus.guicey.jdbi.support.SampleConfiguration

import javax.inject.Inject

class FlywayInitBundle implements GuiceyBundle {

    @Override
    void initialize(GuiceyBootstrap bootstrap) {
        bootstrap.extensions(FlywaySupport)
    }

    @Order(Integer.MIN_VALUE)
    static class FlywaySupport implements Managed {

        @Inject
        SampleConfiguration conf
        Flyway flyway

        @Override
        void start() throws Exception {
            if (flyway != null) {
                return
            }
            DataSourceFactory f = conf.getDatabase();
            flyway = Flyway.configure().cleanDisabled(false)
                    .dataSource(f.getUrl(), f.getUser(), f.getPassword())
                    .load();
            flyway.migrate();
        }

        @Override
        void stop() throws Exception {
            if (flyway != null) {
                flyway.clean()
                flyway = null
            }
        }
    }
}
