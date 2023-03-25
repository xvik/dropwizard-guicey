package ru.vyarus.dropwizard.guice

import io.dropwizard.core.Application
import io.dropwizard.core.Configuration
import io.dropwizard.core.setup.Bootstrap
import io.dropwizard.core.setup.Environment
import ru.vyarus.dropwizard.guice.test.TestSupport
import spock.lang.Specification

/**
 * @author Vyacheslav Rusakov
 * @since 24.04.2018
 */
class CommandInstantiationFailTest extends Specification {

    def "Check not instantiatable command"() {

        when: "run app"
        TestSupport.runCoreApp(App, null)
        then: "error"
        def ex = thrown(IllegalStateException)
        ex.message.startsWith(
                "Failed to instantiate command")
    }

    static class App extends Application<Configuration> {
        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .searchCommands()
                    .enableAutoConfig("ru.vyarus.dropwizard.guice.support.badcmd")
                    .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
        }
    }
}