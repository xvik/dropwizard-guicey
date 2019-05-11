package ru.vyarus.dropwizard.guice

import ch.qos.logback.classic.Level
import io.dropwizard.Application
import io.dropwizard.Configuration
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import ru.vyarus.dropwizard.guice.test.spock.ConfigOverride
import ru.vyarus.dropwizard.guice.test.spock.UseDropwizardApp

/**
 * @author Vyacheslav Rusakov
 * @since 25.04.2019
 */
@UseDropwizardApp(value = App, configOverride =
        @ConfigOverride(key = "logging.loggers.ru\\.vyarus\\.dropwizard\\.guice", value = "DEBUG"))
class JerseyStartupTest extends AbstractTest {

    def "Test jersey startup"() {

        expect:
        true
    }

    static class App extends Application<Configuration> {


        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder().build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
            println "APP RUN"
        }

        @Override
        protected Level bootstrapLogLevel() {
            return Level.DEBUG
        }
    }
}
