package ru.vyarus.dropwizard.guice.examples.util

import io.dropwizard.db.DataSourceFactory
import io.dropwizard.flyway.FlywayFactory
import io.dropwizard.lifecycle.Managed
import org.flywaydb.core.Flyway
import ru.vyarus.dropwizard.guice.examples.JdbiAppConfiguration
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBootstrap
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBundle
import ru.vyarus.dropwizard.guice.module.installer.order.Order

import javax.inject.Inject

/**
 * @author Vyacheslav Rusakov
 * @since 09.12.2016
 */
class FlywayInitBundle implements GuiceyBundle {

    @Override
    void initialize(GuiceyBootstrap bootstrap) {
        bootstrap.extensions(FlywaySupport)
    }

    @Order(Integer.MIN_VALUE)
    static class FlywaySupport implements Managed {

        @Inject
        JdbiAppConfiguration conf
        Flyway flyway

        @Override
        void start() throws Exception {
            if (flyway != null) {
                return
            }
            DataSourceFactory f = conf.getDatabase();
            flyway = new FlywayFactory().build(f.getUrl(), f.getUser(), f.getPassword());
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
