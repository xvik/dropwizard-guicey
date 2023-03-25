package ru.vyarus.dropwizard.guice.lifecycle

import io.dropwizard.core.Application
import io.dropwizard.core.Configuration
import io.dropwizard.core.setup.Bootstrap
import io.dropwizard.core.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.module.lifecycle.GuiceyLifecycleAdapter
import ru.vyarus.dropwizard.guice.module.lifecycle.event.jersey.ApplicationStartedEvent
import ru.vyarus.dropwizard.guice.test.TestSupport
import spock.lang.Specification

/**
 * @author Vyacheslav Rusakov
 * @since 25.10.2019
 */
class StartedEventTest extends Specification {

    def "Check startup event called"() {
        App.started = null

        when: "start-stop with jetty app"
        TestSupport.runWebApp(App, null)
        then: "startup called"
        App.started != null
        App.started
    }

    def "Check startup event called in lightweight tests"() {
        App.started = null

        when: "start-stop without jetty app"
        TestSupport.runCoreApp(App, null)
        then: "start called and test env detected"
        App.started != null
        !App.started
    }

    static class App extends Application<Configuration> {

        static Boolean started

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .listen(new GuiceyLifecycleAdapter() {
                        @Override
                        protected void applicationStarted(ApplicationStartedEvent event) {
                            started = event.isJettyStarted()
                        }
                    })
                    .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {

        }
    }
}
