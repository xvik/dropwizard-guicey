package ru.vyarus.guicey.spa.err

import io.dropwizard.Application
import io.dropwizard.Configuration
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.guicey.spa.SpaBundle
import spock.lang.Specification

/**
 * @author Vyacheslav Rusakov
 * @since 05.04.2017
 */
class SameNameTest extends Specification {

    def "Check duplicate spa names"() {

        when: "starting app"
        new App().run(['server'] as String[])
        then: "error"
        def ex = thrown(IllegalArgumentException)
        ex.message.contains("SPA with name 'app' is already registered")
    }

    static class App extends Application<Configuration> {

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .bundles(
                            SpaBundle.app("app", "/app", "/1").build(),
                            SpaBundle.app("app", "/app", "/2").build())
                    .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
        }
    }
}