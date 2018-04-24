package ru.vyarus.dropwizard.guice

import io.dropwizard.Application
import io.dropwizard.Configuration
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import org.junit.runners.model.Statement
import ru.vyarus.dropwizard.guice.support.feature.DummyPlugin1
import ru.vyarus.dropwizard.guice.test.GuiceyAppRule
import spock.lang.Specification

/**
 * @author Vyacheslav Rusakov
 * @since 24.04.2018
 */
class ExtensionRecognitionFailTest extends Specification {

    def "Check no installer for extension"() {

        when: "run app"
        new GuiceyAppRule(App, null).apply({} as Statement, null).evaluate()
        then: "error"
        def ex = thrown(IllegalStateException)
        ex.cause.cause.message.startsWith(
                "No installer found for extension ru.vyarus.dropwizard.guice.support.feature.DummyPlugin1. Available installers:")
    }

    static class App extends Application<Configuration> {
        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .noDefaultInstallers()
                    .extensions(DummyPlugin1)
                    .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
        }
    }
}
