package ru.vyarus.guicey.jdbi

import ru.vyarus.dropwizard.guice.bundle.lookup.PropertyBundleLookup
import ru.vyarus.guicey.jdbi.support.db.FlywayInitBundle
import spock.lang.Specification

import javax.inject.Inject

/**
 * @author Vyacheslav Rusakov
 * @since 05.12.2016
 */
abstract class AbstractTest extends Specification {

    static {
        PropertyBundleLookup.enableBundles(FlywayInitBundle)
    }

    @Inject
    FlywayInitBundle.FlywaySupport flyway

    void setup() {
        flyway.start()
    }

    void cleanup() {
        flyway.stop()
    }
}