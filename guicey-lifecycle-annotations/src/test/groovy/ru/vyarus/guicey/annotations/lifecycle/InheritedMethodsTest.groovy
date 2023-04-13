package ru.vyarus.guicey.annotations.lifecycle

import io.dropwizard.core.Application
import io.dropwizard.core.Configuration
import io.dropwizard.core.setup.Bootstrap
import io.dropwizard.core.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp
import spock.lang.Specification

import jakarta.annotation.PostConstruct
import jakarta.inject.Inject
import jakarta.inject.Singleton

/**
 * @author Vyacheslav Rusakov
 * @since 27.11.2018
 */
@TestGuiceyApp(App)
class InheritedMethodsTest extends Specification {

    @Inject
    SampleBean bean

    def "Check inherited methods also called"() {

        expect:
        bean.baseCalled
        bean.called
    }

    static class App extends Application<Configuration> {

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap
                    .addBundle(GuiceBundle.builder().build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
        }
    }

    static class BaseBean {
        boolean baseCalled

        @PostConstruct
        void initBase() {
            baseCalled = true
        }
    }

    @Singleton
    static class SampleBean extends BaseBean {
        boolean called

        @PostConstruct
        void init() {
            called = true
        }
    }
}
