package ru.vyarus.dropwizard.guice

import io.dropwizard.Application
import io.dropwizard.Configuration
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import org.junit.runners.model.Statement
import ru.vyarus.dropwizard.guice.module.installer.FeatureInstaller
import ru.vyarus.dropwizard.guice.test.GuiceyAppRule
import spock.lang.Specification


/**
 * @author Vyacheslav Rusakov
 * @since 24.04.2018
 */
class InstallerRegistrationFailTest extends Specification {

    def "Check no installer for extension"() {

        when: "run app"
        new GuiceyAppRule(App, null).apply({} as Statement, null).evaluate()
        then: "error"
        def ex = thrown(IllegalStateException)
        ex.cause.message.startsWith(
                "Failed to register installer")
    }


    static class App extends Application<Configuration> {

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .installers(BadInstaller)
                    .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
        }
    }

    static class BadInstaller implements FeatureInstaller {
        BadInstaller(boolean neee) {
        }

        @Override
        boolean matches(Class type) {
            return false
        }

        @Override
        void report() {

        }
    }
}