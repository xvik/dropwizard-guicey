package ru.vyarus.dropwizard.guice.test

import io.dropwizard.core.Application
import io.dropwizard.core.Configuration
import io.dropwizard.core.setup.Bootstrap
import io.dropwizard.core.setup.Environment
import spock.lang.Specification

/**
 * @author Vyacheslav Rusakov
 * @since 11.07.2016
 */
class GuiceyRuleFailedStartTest extends Specification {

    def "Check failed creation"() {

        when: "start rule for failed app"
        new GuiceyAppRule(FailedApp, null).before()
        then: "error thrown"
        def ex = thrown(IllegalStateException)
        ex.message == "Failed to start test environment"
        ex.cause.message == "Failed to instantiate application"
        ex.cause.cause.message == "Oops"
    }

    def "Check failed startup"() {

        when: "start rule for failed app"
        new GuiceyAppRule(FailedStartApp, null).before()
        then: "error thrown"
        def ex = thrown(IllegalStateException)
        ex.message == "Failed to start test environment"
        ex.cause.message == "Oops"
    }

    static class FailedApp extends Application<Configuration> {
        FailedApp() {
            throw new IllegalStateException("Oops")
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
        }
    }

    static class FailedStartApp extends Application<Configuration> {
        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            throw new IllegalStateException("Oops")
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
        }
    }
}
