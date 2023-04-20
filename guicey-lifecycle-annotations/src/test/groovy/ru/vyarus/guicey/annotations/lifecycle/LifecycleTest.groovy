package ru.vyarus.guicey.annotations.lifecycle

import io.dropwizard.Application
import io.dropwizard.Configuration
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.module.installer.feature.eager.EagerSingleton
import ru.vyarus.dropwizard.guice.test.TestSupport
import spock.lang.Specification

import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

/**
 * @author Vyacheslav Rusakov
 * @since 27.11.2018
 */
class LifecycleTest extends Specification {

    def "Check full lifecycle events"() {

        when:
        SampleBean bean = TestSupport.runWebApp(App, null) {
            it.getInstance(SampleBean)
        }

        then:
        bean.initialized
        bean.started
        bean.destroyed
    }


    def "Check lightweight lifecycle events"() {

        when:
        SampleBean bean = TestSupport.runCoreApp(App, null) {
            it.getInstance(SampleBean)
        }

        then:
        bean.initialized
        !bean.started // no server startup
        bean.destroyed
    }

    static class App extends Application<Configuration> {

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap
                    .addBundle(GuiceBundle.builder()
                    // be sure bean initialized with the context and method would be processed when they appear
                            .extensions(SampleBean)
                            .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
        }
    }

    @EagerSingleton
    static class SampleBean {
        boolean initialized
        boolean started
        boolean destroyed

        @PostConstruct
        private void start() {
            initialized = true
        }

        @PostStartup
        private void afterStartup() {
            started = true
        }

        @PreDestroy
        private void stop() {
            destroyed = true
        }
    }
}
