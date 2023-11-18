package ru.vyarus.dropwizard.guice

import io.dropwizard.core.Application
import io.dropwizard.core.Configuration
import io.dropwizard.core.setup.Bootstrap
import io.dropwizard.core.setup.Environment
import ru.vyarus.dropwizard.guice.support.feature.DummyPlugin1
import ru.vyarus.dropwizard.guice.test.TestSupport
import spock.lang.Specification

/**
 * @author Vyacheslav Rusakov
 * @since 24.04.2018
 */
class ExtensionRecognitionFailTest extends Specification {

    def "Check no installer for extension"() {

        when: "run app"
        TestSupport.runCoreApp(App)
        then: "error"
        def ex = thrown(IllegalStateException)
        ex.message.startsWith(
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
