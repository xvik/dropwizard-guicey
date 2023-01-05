package ru.vyarus.dropwizard.guice

import ch.qos.logback.classic.Level
import io.dropwizard.Application
import io.dropwizard.Configuration
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import ru.vyarus.dropwizard.guice.test.jupiter.TestDropwizardApp

import javax.ws.rs.GET
import javax.ws.rs.Path

/**
 * @author Vyacheslav Rusakov
 * @since 25.04.2019
 */
@TestDropwizardApp(value = App, configOverride = "logging.loggers.ru\\.vyarus\\.dropwizard\\.guice: DEBUG")
class JerseyStartupTest extends AbstractTest {

    def "Test jersey startup"() {

        expect:
        true
    }

    static class App extends Application<Configuration> {


        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
            println "APP RUN"
            environment.jersey().register(FooResource)
        }

        @Override
        protected Level bootstrapLogLevel() {
            return Level.DEBUG
        }
    }

    @Path("/foo/")
    static class FooResource {

        @GET
        String get() {
            return ""
        }
    }
}
