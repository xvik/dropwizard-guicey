package ru.vyarus.dropwizard.guice.examples

import ru.vyarus.dropwizard.guice.bundle.lookup.PropertyBundleLookup
import ru.vyarus.dropwizard.guice.examples.util.FlywayInitBundle
import spock.lang.Specification

import javax.inject.Inject

/**
 * @author Vyacheslav Rusakov
 * @since 01.11.2018
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