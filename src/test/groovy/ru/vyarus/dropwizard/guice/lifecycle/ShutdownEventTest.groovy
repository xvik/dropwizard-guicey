package ru.vyarus.dropwizard.guice.lifecycle

import io.dropwizard.Application
import io.dropwizard.Configuration
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import io.dropwizard.testing.junit.DropwizardAppRule
import org.junit.runners.model.Statement
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.module.lifecycle.GuiceyLifecycleAdapter
import ru.vyarus.dropwizard.guice.module.lifecycle.event.jersey.ApplicationShotdownEvent
import ru.vyarus.dropwizard.guice.module.lifecycle.event.jersey.ApplicationStoppedEvent
import ru.vyarus.dropwizard.guice.test.GuiceyAppRule
import spock.lang.Specification

/**
 * @author Vyacheslav Rusakov
 * @since 25.10.2019
 */
class ShutdownEventTest extends Specification {

    def "Check shutdown event called"() {
        def rule = new DropwizardAppRule(App) {}
        App.shutdown = null
        App.stopped = null

        when: "start-stop with jetty app"
        rule.apply({} as Statement, null).evaluate()
        then: "shutdown called"
        App.shutdown != null
        App.shutdown
        App.stopped != null
        App.stopped
    }

    def "Check shutdown event called in lightweight tests"() {
        def rule = new GuiceyAppRule(App, null)
        App.shutdown = null
        App.stopped = null

        when: "start-stop without jetty app"
        rule.apply({} as Statement, null).evaluate()
        then: "shutdown called"
        App.shutdown != null
        !App.shutdown // indicate called event, but not started server
        App.stopped != null
        App.stopped
    }

    static class App extends Application<Configuration> {

        static Boolean shutdown
        static Boolean stopped

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .listen(new GuiceyLifecycleAdapter() {
                        @Override
                        protected void applicationShutdown(ApplicationShotdownEvent event) {
                            shutdown = event.jettyStarted
                        }

                        @Override
                        protected void applicationStopped(ApplicationStoppedEvent event) {
                            stopped = true
                        }
                    })
                    .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {

        }
    }
}
