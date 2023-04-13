package ru.vyarus.guicey.jdbi3

import ru.vyarus.dropwizard.guice.bundle.lookup.PropertyBundleLookup
import ru.vyarus.guicey.jdbi3.support.db.FlywayInitBundle
import spock.lang.Specification

import jakarta.inject.Inject

/**
 * @author Vyacheslav Rusakov
 * @since 31.08.2018
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